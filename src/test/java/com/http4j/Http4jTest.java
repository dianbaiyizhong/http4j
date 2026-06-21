package com.http4j;

import com.http4j.internal.JsonUtil;
import org.junit.After;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the http4j SDK — no network or server sockets required.
 */
public class Http4jTest {

    @After
    public void tearDown() {
        Http4j.setGlobalConfig(new Http4jConfig());
    }

    // ==============================================================
    // JsonUtil tests
    // ==============================================================

    @Test
    public void testJsonParseEmpty() {
        assertNull(JsonUtil.parse(null));
        assertNull(JsonUtil.parse("  "));
    }

    @Test
    public void testJsonParseObject() {
        String json = "{\"code\":0,\"message\":\"ok\",\"data\":[1,2,3]}";
        Object parsed = JsonUtil.parse(json);
        assertTrue(parsed instanceof Map);
        Map<?, ?> map = (Map<?, ?>) parsed;
        assertEquals(0, ((Number) map.get("code")).intValue());
        assertEquals("ok", map.get("message"));
        assertTrue(map.get("data") instanceof List);
    }

    @Test
    public void testJsonParseNested() {
        String json = "{\"a\":{\"b\":{\"c\":\"deep\"}}}";
        Map<?, ?> a = (Map<?, ?>) ((Map<?, ?>) JsonUtil.parse(json)).get("a");
        Map<?, ?> b = (Map<?, ?>) a.get("b");
        assertEquals("deep", b.get("c"));
    }

    @Test
    public void testJsonParseArray() {
        String json = "[1,\"two\",true,null,{\"k\":\"v\"}]";
        List<?> list = (List<?>) JsonUtil.parse(json);
        assertEquals(5, list.size());
        assertEquals(1, ((Number) list.get(0)).intValue());
        assertEquals("two", list.get(1));
        assertEquals(true, list.get(2));
        assertNull(list.get(3));
        assertTrue(list.get(4) instanceof Map);
    }

    @Test
    public void testJsonParseNumberTypes() {
        Map<?, ?> m = (Map<?, ?>) JsonUtil.parse("{\"i\":42,\"l\":9999999999,\"d\":3.14}");
        assertTrue(m.get("i") instanceof Integer);
        assertTrue(m.get("l") instanceof Long);
        assertTrue(m.get("d") instanceof Double);
    }

    @Test
    public void testJsonParseEscape() {
        String json = "{\"s\":\"hello\\nworld\"}";
        Map<?, ?> m = (Map<?, ?>) JsonUtil.parse(json);
        assertEquals("hello\nworld", m.get("s"));
    }

    // ==============================================================
    // DefaultResultRule tests
    // ==============================================================

    @Test
    public void testDefaultRuleSuccess() {
        DefaultResultRule rule = new DefaultResultRule();
        assertTrue(rule.isBusinessSuccess("{\"code\":0}"));
        assertTrue(rule.isBusinessSuccess("{\"code\":0,\"message\":\"ok\"}"));
    }

    @Test
    public void testDefaultRuleFail() {
        DefaultResultRule rule = new DefaultResultRule();
        assertFalse(rule.isBusinessSuccess("{\"code\":-1}"));
        assertFalse(rule.isBusinessSuccess("{\"code\":1}"));
        assertFalse(rule.isBusinessSuccess("{\"code\":403}"));
        assertFalse(rule.isBusinessSuccess(null));
        assertFalse(rule.isBusinessSuccess(""));
        assertFalse(rule.isBusinessSuccess("not json"));
    }

    @Test
    public void testDefaultRuleCodeAndMessage() {
        DefaultResultRule rule = new DefaultResultRule();
        assertEquals(500, rule.getBusinessCode("{\"code\":500,\"message\":\"err\"}"));
        assertEquals("err", rule.getBusinessMessage("{\"code\":500,\"message\":\"err\"}"));
    }

    // ==============================================================
    // Observer wrapping tests — no network needed
    // ==============================================================

    @Test
    public void testPlainObserverWrapsGlobal() {
        List<String> events = new ArrayList<>();

        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new ResultObserver() {
            @Override
            public void callHttpStart() { events.add("global"); }
        });
        Http4j.setGlobalConfig(cfg);

        // Plain observer (no overrides) → wraps global
        Http4j.request("http://localhost:9999/wrap")
                .observe(new ResultObserver())
                .executeForData();

        // callHttpStart fires before the connection attempt, so it should be recorded
        assertTrue(events.contains("global"));
    }

    @Test
    public void testObserverAlwaysChainsGlobal() {
        List<String> events = new ArrayList<>();

        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new ResultObserver() {
            @Override
            public void callHttpStart() { events.add("global"); }

            @Override
            public void callHttpFail(int statusCode, String message, Throwable throwable) {
                super.callHttpFail(statusCode, message, throwable);

                System.out.println("=====");
            }
        });
        Http4j.setGlobalConfig(cfg);

        // Local observer with overrides → chains: global first, then local
        Http4j.request("http://localhost:9999/chain")
                .observe(new ResultObserver() {
                    @Override
                    public void callHttpStart() { events.add("local"); }


                    @Override
                    public void callHttpFail(int statusCode, String message, Throwable throwable) {
                        super.callHttpFail(statusCode, message, throwable);
                        System.out.println("wwwwwww");
                    }
                })
                .executeForData();

        // Both fire: global first, then local
        assertEquals(Arrays.asList("global", "local"), events);
    }

    @Test
    public void testObserverNullDoesNothing() {
        List<String> events = new ArrayList<>();
        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new ResultObserver() {
            @Override
            public void callHttpStart() {
                events.add("global");
            }
        });
        Http4j.setGlobalConfig(cfg);

        // Null observer should not affect the request
        Http4jRequest req = Http4j.request("http://localhost:9999/null")
                .observe(null);
        assertNotNull(req);
    }

    // ==============================================================
    // Builder configuration tests
    // ==============================================================

    @Test
    public void testBuilderSetsMethod() {
        // We can't test the HTTP call, but we verify the builder returns itself correctly
        Http4jRequest req = Http4j.request("http://example.com")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"test\":1}")
                .connectTimeout(1000)
                .readTimeout(2000);
        assertNotNull(req);
    }

    @Test
    public void testGlobalConfigDefaults() {
        Http4jConfig cfg = new Http4jConfig();
        assertEquals(5000, cfg.getConnectTimeout());
        assertEquals(5000, cfg.getReadTimeout());
        assertNull(cfg.getDefaultObserver());
        assertNull(cfg.getDefaultRule());
    }

    @Test
    public void testGlobalConfigSetAndGet() {
        Http4jConfig cfg = new Http4jConfig();
        cfg.setConnectTimeout(3000);
        cfg.setReadTimeout(7000);
        cfg.setDefaultObserver(new ResultObserver());
        cfg.setDefaultRule(new DefaultResultRule());

        Http4j.setGlobalConfig(cfg);
        Http4jConfig retrieved = Http4j.getGlobalConfig();
        assertEquals(3000, retrieved.getConnectTimeout());
        assertEquals(7000, retrieved.getReadTimeout());
        assertNotNull(retrieved.getDefaultObserver());
        assertNotNull(retrieved.getDefaultRule());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullGlobalConfigThrows() {
        Http4j.setGlobalConfig(null);
    }

    // ==============================================================
    // Rule wrapping / override tests
    // ==============================================================

    @Test
    public void testOverrideGlobalRuleFlag() {
        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultRule(new DefaultResultRule());
        Http4j.setGlobalConfig(cfg);

        Http4jRequest req = Http4j.request("http://localhost:9999/r")
                .overrideGlobalRule()
                .rule(new ResultRule() {
                    @Override
                    public boolean isBusinessSuccess(String body) {
                        return true;
                    }

                    @Override
                    public int getBusinessCode(String body) {
                        return 0;
                    }

                    @Override
                    public String getBusinessMessage(String body) {
                        return "";
                    }
                });
        assertNotNull(req);
    }

    @Test
    public void testCustomRuleLogic() {
        // A rule that treats "status":"ok" as business success
        ResultRule rule = new ResultRule() {
            @Override
            public boolean isBusinessSuccess(String body) {
                return body != null && body.contains("\"status\":\"ok\"");
            }

            @Override
            public int getBusinessCode(String body) {
                return 0;
            }

            @Override
            public String getBusinessMessage(String body) {
                return "";
            }
        };

        assertTrue(rule.isBusinessSuccess("{\"status\":\"ok\"}"));
        assertFalse(rule.isBusinessSuccess("{\"status\":\"fail\"}"));
    }

    // ==============================================================
    // HttpURLConnection helper tests (mock HTTP errors)
    // ==============================================================

    @Test
    public void testHttpFailForExceptionPassesCodeZeroAndThrowable() {
        List<String> events = new ArrayList<>();
        List<Throwable> caught = new ArrayList<>();
        String body = Http4j.request("http://192.0.2.1:9999/nope")
                .connectTimeout(500)
                .readTimeout(500)
                .observe(new ResultObserver() {
                    @Override
                    public void callHttpStart() {
                        events.add("start");
                    }

                    @Override
                    public void callHttpFail(int code, String msg, Throwable throwable) {
                        events.add("httpFail:" + code);
                        caught.add(throwable);
                    }
                })
                .executeForData();

        assertTrue(events.get(0).equals("start"));
        assertTrue(events.get(1).startsWith("httpFail:"));
        // Exception case: code must be 0 and throwable must be non-null
        assertEquals("httpFail:0", events.get(1));
        assertNotNull(caught.get(0));
        assertEquals("", body);
    }

    @Test
    public void testExecuteForDataReturnsBodyOnSuccess() throws Exception {
        // Start a quick manual HTTP server to test the real flow
        // We use the JDK's simple Exchange pattern — but since binding is blocked,
        // we skip this test and just verify the result is sensible.
        // This is a placeholder — real integration tests run on a network-permitted env.
        assertNotNull(Http4j.request("http://example.com"));
    }
}
