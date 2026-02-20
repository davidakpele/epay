package com.payrix.administrator.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.payrix.administrator.dtos.DashboardData;
import com.payrix.administrator.httpClients.DashboardAggregationService;
import com.payrix.administrator.httpClients.UserServiceClient;
import com.payrix.administrator.models.User;
import com.payrix.administrator.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final UserServiceClient userService;
    private final DashboardAggregationService dashboardAggregationService;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public AdminController(UserServiceClient userService, DashboardAggregationService dashboardAggregationService) {
        this.userService = userService;
        this.dashboardAggregationService = dashboardAggregationService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model, HttpServletRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        // Get token from session
        HttpSession session = request.getSession(false);
        String token = (session != null) ? (String) session.getAttribute("jwtToken") : "";
        
        // Rest of your code stays the same...
        DashboardData dashboardData = dashboardAggregationService
            .fetchDashboardData(token)
            .block();
        
        logger.info("Dashboard Response - Users: {}, Revenue: {}, Transactions: {}/{}, Blacklisted: {}, Escrow Pending: {}", 
            dashboardData.getTotalUsers(),
            dashboardData.getTotalRevenue(),
            dashboardData.getTransactionStats().getPending(),
            dashboardData.getTransactionStats().getTotal(),
            dashboardData.getBlacklistStats().getTotal(),
            dashboardData.getPendingTransactionStats().getPending());

        model.addAttribute("totalUsers", dashboardData.getTotalUsers());
        model.addAttribute("totalRevenue", dashboardData.getTotalRevenue());
        model.addAttribute("totalTransactions", dashboardData.getTransactionStats().getTotal());
        model.addAttribute("pendingTransactions", dashboardData.getTransactionStats().getPending());
        model.addAttribute("blacklistedAccounts", dashboardData.getBlacklistStats().getTotal());
        model.addAttribute("escrowPendingTransactions", dashboardData.getPendingTransactionStats().getPending());
        
        if (currentUser != null) {
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("fullName", currentUser.getFirstName() + " " + currentUser.getLastName());
            model.addAttribute("email", currentUser.getEmail());
            model.addAttribute("role", currentUser.getRole());
        }
        return "admin/index";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/users/index";
    }

    @GetMapping("/liquidity")
    @PreAuthorize("hasRole('ADMIN')")
    public String liquidity(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/liquidity";
    }

    @GetMapping("/card-request")
    @PreAuthorize("hasRole('ADMIN')")
    public String cardRequests(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/cardRequest";
    }

    @GetMapping("/loan-request")
    @PreAuthorize("hasRole('ADMIN')")
    public String loanRequests(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/loanRequest";
    }

    @GetMapping("/account-audit")
    @PreAuthorize("hasRole('ADMIN')")
    public String accountAudit(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/accountAudit";
    }

    @GetMapping("/settings/currencySwap/control")
    @PreAuthorize("hasRole('ADMIN')")
    public String currencySwapControl(Model model) {
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        return "admin/currencySwap";
    }

    @GetMapping("/user/{id}/view")
    @PreAuthorize("hasRole('ADMIN')")
    public String user(@PathVariable Long id, Model model){
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        model.addAttribute("userId", id);
        return "admin/users/user-profile";
    }

    @GetMapping("/user/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUser(@PathVariable Long id, Model model){
        String username = SecurityUtil.getCurrentUsername();
        model.addAttribute("username", username != null ? username : "Admin");
        model.addAttribute("userId", id);
        return "admin/users/edit-user-profile";
    }

}