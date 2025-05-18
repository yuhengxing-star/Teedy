package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserRegisterRequestDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.docs.core.constant.Constants;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import java.util.Date;
import java.util.List;
import java.util.Map;      
import java.util.HashMap; 
import java.util.stream.Collectors;
import java.util.UUID;
import java.text.SimpleDateFormat;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/user/register_request")
@Produces(MediaType.APPLICATION_JSON)
public class UserRegisterRequestResource extends BaseResource {
    private EntityManager em = Persistence.createEntityManagerFactory("transactions-optional").createEntityManager();
    private UserRegisterRequestDao requestDao = new UserRegisterRequestDao(em);
    @Context
    private SecurityContext securityContext;

    private Response buildSuccess(String message) {
        return Response.ok().type(MediaType.APPLICATION_JSON)
            .entity("{\"success\":true,\"message\":\"" + message + "\"}")
            .build();
    }
    private Response buildError(int status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
            .entity("{\"success\":false,\"message\":\"" + message + "\"}")
            .build();
    }

    /**
     * 访客提交注册请求
     */
    @POST
    @Path("")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitRequest(@FormParam("username") String username,
                                  @FormParam("password") String password,
                                  @FormParam("email") String email) {
        // 检查用户名、邮箱唯一性
        if (em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username).getSingleResult() > 0) {
            return buildError(400, "用户名已存在");
        }
        if (em.createQuery("SELECT COUNT(r) FROM UserRegisterRequest r WHERE r.username = :username AND r.status = 'PENDING'", Long.class)
                .setParameter("username", username).getSingleResult() > 0) {
            return buildError(400, "该用户名的注册请求正在审核中");
        }
        UserRegisterRequest req = new UserRegisterRequest();
        req.setId(UUID.randomUUID().toString());
        req.setUsername(username);
        req.setPassword(password); // 实际项目应加密
        req.setEmail(email);
        req.setStatus("PENDING");
        req.setCreateDate(new Date());
        requestDao.create(req);
        return buildSuccess("注册请求已提交，等待管理员审核");
    }

    /**
     * 管理员获取所有待审核注册请求
     */
    @GET
    @Path("/list")
    @RolesAllowed({"ADMIN"})
    public Response listPendingRequests() {
        try {
            List<UserRegisterRequest> list = requestDao.findAllPending();
            jakarta.json.JsonArrayBuilder arrayBuilder = jakarta.json.Json.createArrayBuilder();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (UserRegisterRequest r : list) {
                arrayBuilder.add(jakarta.json.Json.createObjectBuilder()
                    .add("id", r.getId() == null ? "" : r.getId())
                    .add("username", r.getUsername() == null ? "" : r.getUsername())
                    .add("email", r.getEmail() == null ? "" : r.getEmail())
                    .add("createDate", r.getCreateDate() == null ? "" : sdf.format(r.getCreateDate()))
                    .add("decisionDate", r.getDecisionDate() == null ? "" : sdf.format(r.getDecisionDate()))
                );
            }
            return Response.ok(arrayBuilder.build()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 管理员审核注册请求（通过）
     */
    @POST
    @Path("/{id}/approve")
    @RolesAllowed({"ADMIN"})
    public Response approveRequest(@PathParam("id") String id) {
        System.out.println("[approveRequest] 审核通过请求ID: " + id);
        UserRegisterRequest req = requestDao.findById(id);
        if (req == null || !"PENDING".equals(req.getStatus())) {
            System.out.println("[approveRequest] 请求不存在或已处理");
            return buildError(404, "请求不存在或已处理");
        }
        // 创建正式用户
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // 实际项目应加密
        user.setEmail(req.getEmail());
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setOnboarding(true);
        user.setStorageQuota(100_000L);
        String adminId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        try {
            System.out.println("[approveRequest] 创建正式用户: " + user.getUsername());
            userDao.create(user, adminId);
        } catch (Exception e) {
            System.out.println("[approveRequest] 创建用户异常: " + e.getMessage());
            e.printStackTrace();
            return buildError(400, e.getMessage());
        }
        // 更新请求状态
        req.setStatus("APPROVED");
        req.setDecisionDate(new Date());
        req.setDecisionAdminId(adminId);
        requestDao.update(req);
        System.out.println("[approveRequest] 审核通过完成: " + id);
        return buildSuccess("已通过注册请求");
    }

    /**
     * 管理员审核注册请求（拒绝）
     */
    @POST
    @Path("/{id}/reject")
    @RolesAllowed({"ADMIN"})
    public Response rejectRequest(@PathParam("id") String id) {
        System.out.println("[rejectRequest] 审核拒绝请求ID: " + id);
        UserRegisterRequest req = requestDao.findById(id);
        if (req == null || !"PENDING".equals(req.getStatus())) {
            System.out.println("[rejectRequest] 请求不存在或已处理");
            return buildError(404, "请求不存在或已处理");
        }
        String adminId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        req.setStatus("REJECTED");
        req.setDecisionDate(new Date());
        req.setDecisionAdminId(adminId);
        requestDao.update(req);
        System.out.println("[rejectRequest] 审核拒绝完成: " + id);
        return buildSuccess("已拒绝注册请求");
    }

    // DTO类（只保留在resource中）
    public static class UserRegisterRequestDto {
        public String id;
        public String username;
        public String email;
        public String createDate;
        public String decisionDate;
        public UserRegisterRequestDto(UserRegisterRequest r) {
            this.id = r.getId();
            this.username = r.getUsername();
            this.email = r.getEmail();
            this.createDate = r.getCreateDate() == null ? null :
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getCreateDate());
            this.decisionDate = r.getDecisionDate() == null ? null :
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getDecisionDate());
        }
    }
}
 