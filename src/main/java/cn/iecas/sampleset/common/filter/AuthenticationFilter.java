package cn.iecas.sampleset.common.filter;


import cn.iecas.sampleset.pojo.dto.UserInfo;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
import cn.iecas.sampleset.service.UserInfoService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * token过滤器，验证token并将userid和username加入请求参数
 */
@Configuration
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        CommonResult commonResult = userInfoService.getUserInfo(token);

        if (commonResult.getData() == null){
            response.getWriter().write(JSONObject.toJSONString(commonResult,SerializerFeature.WriteMapNullValue));
        }

        UserInfo userInfo = (UserInfo) commonResult.getData();
        int userId = userInfo.getId();
        String userName = userInfo.getName();



        RequestWrapper requestWrapper = null;
        if (request.getMethod().toUpperCase().equals("GET")){
            requestWrapper = new RequestWrapper(request);
            requestWrapper.addParameter("userId",userId);
            requestWrapper.addParameter("userName",userName);
        }else {
            String line;
            StringBuilder content = new StringBuilder();
            while((line = request.getReader().readLine())!=null){
                content.append(line);
            }

            JSONObject body = JSONObject.parseObject(content.toString());
            body.put("userId",userId);
            body.put("userName",userName);
            requestWrapper = new RequestWrapper(request,body.toJSONString().getBytes());
        }


        filterChain.doFilter(requestWrapper,response);
    }

    static class RequestWrapper extends HttpServletRequestWrapper{
        private byte[] body;
        private Map<String,String[]> params = new HashMap<>();


        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public RequestWrapper(HttpServletRequest request) {
            super(request);
            this.params.putAll(request.getParameterMap());
        }

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public RequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
            this.params.putAll(request.getParameterMap());
            //request.get
        }


        /**
         * 在获取所有的参数名,必须重写此方法，否则对象中参数值映射不上
         *
         * @return
         */
        @Override
        public Enumeration<String> getParameterNames() {
            return new Vector<>(params.keySet()).elements();
        }

        /**
         * 重写getParameter方法
         *
         * @param name 参数名
         * @return 返回参数值
         */
        @Override
        public String getParameter(String name) {
            String[] values = params.get(name);
            if (values == null || values.length == 0) {
                return null;
            }
            return values[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = params.get(name);
            if (values == null || values.length == 0) {
                return null;
            }
            return values;
        }

        /**
         * 增加多个参数
         *
         * @param otherParams 增加的多个参数
         */
        public void addAllParameters(Map<String, Object> otherParams) {
            for (Map.Entry<String, Object> entry : otherParams.entrySet()) {
                addParameter(entry.getKey(), entry.getValue());
            }
        }

        /**
         * 增加参数
         *
         * @param name  参数名
         * @param value 参数值
         */
        public void addParameter(String name, Object value) {
            if (value != null) {
                if (value instanceof String[]) {
                    params.put(name, (String[]) value);
                } else if (value instanceof String) {
                    params.put(name, new String[]{(String) value});
                } else {
                    params.put(name, new String[]{String.valueOf(value)});
                }
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException{
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener listener) {

                }

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException{
            String enc = getCharacterEncoding();
            if (enc == null) enc = "UTF-8";
            return new BufferedReader(new InputStreamReader(getInputStream(),enc));
        }


    }
}
