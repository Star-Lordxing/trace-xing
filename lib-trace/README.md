# 调用链使用
## 接入
引入lib包即可：
```xml
		<dependency>
			<groupId>com.kingdee.finance</groupId>
			<artifactId>sscf-games-lib-trace</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
```

## 结果二次校验
当需要根据Rpc、restTemplate、RedisTemplate等返回值进行二次校验时，
只需要实现TraceResultValidator 接口 或继承DefaultTraceResultValidator（默认），如：
```java
@Component
public class RpcTraceResultValidator extends DefaultTraceResultValidator {

    @Override
    public boolean validate(TraceSpan traceSpan, Object result) {
        if (traceSpan.getTraceTypeEnum() == TraceTypeEnum.RPC_CONSUMER
                && result instanceof RpcRespDTO) {
            RpcRespDTO rpcRespDTO = (RpcRespDTO) result;
            if ("999999".equals(rpcRespDTO.getCode())) {
                return false;
            }
        }
        return super.validate(traceSpan, result);
    }
}
```
返回false表示验证不通过，调用的结果类型会被标记成"result_error"

## Http Header过滤
调用链会记录Http请求的头部，但是会过滤掉部分通用的意义不大的请求头(参考com.sscf.games.lib.trace.common.DefaultHttpHeaderFilter).
需要自定义过滤时，只需要实现com.sscf.games.lib.trace.common.HttpHeaderFilter接口，然后注册为Spring Bean即可