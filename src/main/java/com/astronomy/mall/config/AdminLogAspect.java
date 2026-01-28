package com.astronomy.mall.config;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.module.admin.entity.AdminLogEntity;
import com.astronomy.mall.module.admin.service.AdminLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 管理员操作日志AOP切面
 *
 * 📌 功能说明:
 * 1. 拦截所有标注了 @AdminLog 注解的方法
 * 2. 自动记录操作日志到数据库
 * 3. 记录操作人、时间、参数、IP、执行时长等
 *
 * 📌 执行流程:
 * @AdminLog → AdminLogAspect.around() → 记录开始时间 → 执行业务方法 → 计算耗时 → 保存日志
 *
 * 📌 后续模块使用:
 * 所有 AdminXxxController 的方法都可以使用 @AdminLog 注解自动记录日志
 */
@Slf4j
@Aspect
@Component
public class AdminLogAspect {

    @Autowired
    private AdminLogService adminLogService;

    /**
     * 环绕通知: 拦截所有 @AdminLog 注解的方法
     */
    @Around("@annotation(com.astronomy.mall.common.annotation.AdminLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        long startTime = System.currentTimeMillis();

        // 1. 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取请求上下文,跳过日志记录");
            return point.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 2. 获取注解信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        AdminLog adminLog = method.getAnnotation(AdminLog.class);

        // 3. 创建日志对象
        AdminLogEntity logEntity = new AdminLogEntity();

        // 管理员信息 (从request中获取,AdminInterceptor已存入)
        Long adminId = (Long) request.getAttribute("adminId");
        String adminName = (String) request.getAttribute("adminName");
        logEntity.setAdminId(adminId);
        logEntity.setAdminName(adminName);

        // 操作信息
        logEntity.setOperation(adminLog.value());
        logEntity.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        // 请求参数 (如果注解配置了记录参数)
        if (adminLog.recordParams()) {
            Object[] args = point.getArgs();
            try {
                String params = JSON.toJSONString(args);
                // 限制参数长度,避免存储过大
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                logEntity.setParams(params);
            } catch (Exception e) {
                log.error("参数序列化失败", e);
                logEntity.setParams("参数序列化失败: " + e.getMessage());
            }
        }

        // IP地址
        logEntity.setIpAddress(getIpAddress(request));

        // User-Agent
        logEntity.setUserAgent(request.getHeader("User-Agent"));

        // 4. 执行业务方法
        Object result = null;
        try {
            result = point.proceed();
            logEntity.setStatus(1); // 成功
        } catch (Exception e) {
            logEntity.setStatus(0); // 失败
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            logEntity.setErrorMsg(errorMsg);
            throw e; // 继续抛出异常,不影响业务
        } finally {
            // 5. 计算执行时长
            long endTime = System.currentTimeMillis();
            logEntity.setExecutionTime((int) (endTime - startTime));

            // 6. 异步保存日志 (避免影响业务性能)
            try {
                adminLogService.save(logEntity);
                log.info("管理员操作日志已保存: {}", adminLog.value());
            } catch (Exception e) {
                log.error("保存管理员日志失败", e);
                // 日志保存失败不影响业务
            }
        }

        return result;
    }

    /**
     * 获取真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况,取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}