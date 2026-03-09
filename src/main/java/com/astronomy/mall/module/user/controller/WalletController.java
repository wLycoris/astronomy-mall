package com.astronomy.mall.module.user.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.user.dto.RechargeDTO;
import com.astronomy.mall.module.user.dto.WithdrawDTO;
import com.astronomy.mall.module.user.entity.BalanceLog;
import com.astronomy.mall.module.user.mapper.BalanceLogMapper;
import com.astronomy.mall.module.user.service.BalanceService;
import com.astronomy.mall.module.user.vo.WalletVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 钱包控制器
 *
 * 接口列表（4个）：
 *   GET  /api/user/wallet               - 查询余额 + 近20条流水
 *   GET  /api/user/balance-log/list     - 完整流水列表（分页）
 *   POST /api/user/wallet/recharge      - 模拟充值
 *   POST /api/user/wallet/withdraw      - 模拟提现
 *
 * 文件路径: com.astronomy.mall.module.user.controller.WalletController
 *
 * 📌 userId 从 request.getAttribute("userId") 取，
 *    由 JwtInterceptor 在 preHandle 中写入，与其他 Controller 保持一致
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class WalletController {

    private final BalanceService   balanceService;
    private final BalanceLogMapper balanceLogMapper;

    // ──────────────────────────────────────────────
    // 1. 查询余额 + 近20条流水（钱包首页）
    // ──────────────────────────────────────────────

    @GetMapping("/wallet")
    public Result<WalletVO> getWallet(HttpServletRequest request) {
        Long userId = getUserId(request);

        WalletVO vo = new WalletVO();
        vo.setBalance(balanceService.getBalance(userId));
        List<BalanceLog> logs = balanceLogMapper.selectRecentByUserId(userId, 20);
        vo.setLogs(logs);

        return Result.success(vo);
    }

    // ──────────────────────────────────────────────
    // 2. 完整流水列表（分页）
    // ──────────────────────────────────────────────

    @GetMapping("/balance-log/list")
    public Result<IPage<BalanceLog>> getBalanceLogList(
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Long userId = getUserId(request);
        IPage<BalanceLog> page = balanceService.getBalanceLogPage(userId, pageNum, pageSize);
        return Result.success(page);
    }

    // ──────────────────────────────────────────────
    // 3. 模拟充值
    // ──────────────────────────────────────────────

    @PostMapping("/wallet/recharge")
    public Result<String> recharge(@Validated @RequestBody RechargeDTO dto,
                                   HttpServletRequest request) {
        Long userId = getUserId(request);
        balanceService.changeBalance(
                userId,
                dto.getAmount(),
                1,
                "模拟充值 ¥" + dto.getAmount().toPlainString(),
                null,
                "recharge"
        );
        return Result.success("充值成功");
    }

    // ──────────────────────────────────────────────
    // 4. 模拟提现
    // ──────────────────────────────────────────────

    @PostMapping("/wallet/withdraw")
    public Result<String> withdraw(@Validated @RequestBody WithdrawDTO dto,
                                   HttpServletRequest request) {
        Long userId = getUserId(request);
        balanceService.changeBalance(
                userId,
                dto.getAmount().negate(),
                2,
                "模拟提现 ¥" + dto.getAmount().toPlainString(),
                null,
                "withdraw"
        );
        return Result.success("提现成功");
    }

    // ──────────────────────────────────────────────
    // 工具方法
    // ──────────────────────────────────────────────

    /**
     * 从 request attribute 取 userId
     * JwtInterceptor.preHandle() 已通过 request.setAttribute("userId", userId) 写入
     */
    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}