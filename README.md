# http4j

写一个maven构建的java工程，jdk8以上均可使用，要求不能引入其他依赖

功能如下：

1. 他是http请求工具sdk
2. 希望可以让客户端调用的时候，可以根据返回结果进行规则自定义，例如返回body里的code=0，才是callBusinessSuccess，然后我还能统一处理callBusinessSuccess等；然后我也能在调用的时候，进行类似rxjava那样对callBusinessSuccess进行自己的逻辑处理，当然，也可以选择是基于全局基础上，还是完全覆盖



```java
        List<UserInfo> userInfos = Http4j.request(url)
                .observe(new MyResultObserver() {
                    @Override
                    public void callBusinessFail(int code, String messages) {
                        super.callBusinessFail(code, messages);
                        log.info("=====业务请求失败了，我要发送消息队列...todo");
                    }

                    @Override
                    public void callHttpSuccess() {
                        super.callHttpSuccess();
                    }
                })
                .executeForData();
```

