package com.http4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JunitTest {

    @Test
    public void test_simple() {
        String body = Http4j.request("http://localhost:8000/api/users")
                .execute();

        System.out.println(body);
    }

    @Test
    public void test_simple_baseUrl() {
        String body = Http4j.request("/api/users")
                .setBaseUrl("http://localhost:8000")
                .execute();

        System.out.println(body);
    }


    @Test
    public void test_http_fail() {

        assertThrows(Http4jException.class, () -> {
            String body = Http4j.request("/api/users")
                    .setBaseUrl("http://localhost:8001")  // 故意写错地址，报错
                    .execute();
        });

    }


    @Test
    public void test_http_observer() {

        String body = Http4j.request("/api/users")
                .setObserver(new MyResultObserver())
                .execute();

    }


}
