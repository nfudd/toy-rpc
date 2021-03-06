package com.sinjinsong.toy.protocol.injvm;

import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.common.util.InvokeParamUtil;
import com.sinjinsong.toy.protocol.api.InvokeParam;
import com.sinjinsong.toy.protocol.api.support.AbstractInvoker;
import com.sinjinsong.toy.common.domain.GlobalRecycler;
import com.sinjinsong.toy.common.domain.RPCResponse;

import java.lang.reflect.Method;

/**
 * @author sinjinsong
 * @date 2018/7/18
 */
public class InJvmInvoker<T> extends AbstractInvoker<T> {

    @Override
    public RPCResponse invoke(InvokeParam invokeParam) throws RPCException {
        Object serviceBean = getGlobalConfig().getProtocol().referLocalService(invokeParam.getInterfaceName()).getRef();
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = invokeParam.getMethodName();
        Class<?>[] parameterTypes = invokeParam.getParameterTypes();
        Object[] parameters = invokeParam.getParameters();
        RPCResponse response = GlobalRecycler.reuse(RPCResponse.class);
        response.setRequestId(invokeParam.getRequestId());
        try {
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(serviceBean, parameters);
            response.setResult(result);
            // 回收request
            InvokeParamUtil.extractRequestFromInvokeParam(invokeParam).recycle();
        } catch (Throwable t) {
            t.printStackTrace();
            response.setCause(t);
        }
        return response;
    }

}
