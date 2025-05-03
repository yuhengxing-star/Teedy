package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.authentication.InternalAuthenticationHandler;

//import antlr.collections.List;
import jakarta.persistence.NoResultException;

import com.sismics.docs.core.dao.GroupDao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.UUID;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestGD extends BaseTransactionalTest{
    GroupDao groupDao = new GroupDao();

    @Test
    public void testGetActiveByName_Exists() {
        Group group = new Group();
        group.setName("TestGroup");
        groupDao.create(group, "user1");

        // 调用方法
        Group result = groupDao.getActiveByName("TestGroup");
        assertNotNull(result);
        assertEquals("TestGroup", result.getName());
    }

    @Test
    public void testGetActiveById_Exists() {
        // 准备测试数据
        Group group = new Group();
        group.setName("TestIDGroup");
        String groupId = groupDao.create(group, "user1");

        // 调用方法
        Group result = groupDao.getActiveById(groupId);
        assertNotNull(result);
        assertEquals(groupId, result.getId());
    }

    @Test
    public void testGetActiveById_NotExists() {
        // 调用方法（使用随机不存在的 ID）
        Group result = groupDao.getActiveById(UUID.randomUUID().toString());
        assertNull(result);
    }

    @Test
    public void testCreateGroup_Success() {
        // 创建群组
        Group group = new Group();
        group.setName("NewGroup");
        String groupId = groupDao.create(group, "user1");

        // 验证 ID 非空
        assertNotNull(groupId);

        // 验证数据库记录
        Group savedGroup = groupDao.getActiveById(groupId);
        assertEquals("NewGroup", savedGroup.getName());

    }

    @Test
    public void testDeleteGroup_Success() {
        // 创建群组并关联用户、ACL、子群组
        Group group = new Group();
        group.setName("TestDeleteGroup");
        String groupId = groupDao.create(group, "user1");
        groupDao.addMember(new UserGroup());

        // 删除群组
        groupDao.delete(groupId, "user1");

        // 验证群组被标记删除
        Group deletedGroup = groupDao.getActiveById(groupId);
        assertNull(deletedGroup); // 因为 getActiveById 会过滤已删除的


    }

    @Test(expected = NoResultException.class)
    public void testDeleteGroup_NotExists() {
        groupDao.delete("invalid-group-id", "user1");
    }

    @Test
    public void testFindByCriteria_BaseQuery() {
        // 插入测试数据
        Group group = new Group();
        group.setName("TestGroup");
        groupDao.create(group, "user1");

        // 构建无条件的查询
        GroupCriteria criteria = new GroupCriteria();
        List<GroupDto> result = groupDao.findByCriteria(criteria, null);

        // 验证返回结果包含所有群组
        assertFalse(result.isEmpty());
        assertEquals("TestGroup", result.get(0).getName());
    }

    @Test
    public void testFindByCriteria_WithSearch() {
        // 插入匹配的群组
        Group group1 = new Group();
        group1.setName("AlphaGroup");
        groupDao.create(group1, "user1");

        // 插入不匹配的群组
        Group group2 = new Group();
        group2.setName("BetaGroup");
        groupDao.create(group2, "user1");

        // 设置搜索条件
        GroupCriteria criteria = new GroupCriteria()
                .setSearch("alpha");
        List<GroupDto> result = groupDao.findByCriteria(criteria, null);

        // 验证结果过滤正确
        assertEquals(1, result.size());
        assertEquals("AlphaGroup", result.get(0).getName());
    }

    @Test
    public void testFindByCriteria_WithUserIdNonRecursive() {
        // 创建群组并关联用户
        Group group = new Group();
        String groupId = groupDao.create(group, "user1");
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(groupId);
        userGroup.setUserId("user2");
        groupDao.addMember(userGroup);

        // 设置查询条件
        GroupCriteria criteria = new GroupCriteria()
                .setUserId("user2")
                .setRecursive(false);
        List<GroupDto> result = groupDao.findByCriteria(criteria, null);

        // 验证只返回直接关联的群组
        assertEquals(1, result.size());
        assertEquals(groupId, result.get(0).getId());
    }

    @Test
    public void testFindByCriteria_WithUserIdRecursive() {
        // 创建父子群组结构
        Group parentGroup = new Group();
        parentGroup.setName("Parent");
        String parentId = groupDao.create(parentGroup, "user1");

        Group childGroup = new Group();
        childGroup.setName("Child");
        childGroup.setParentId(parentId);
        String childId = groupDao.create(childGroup, "user1");

        // 关联用户到子群组
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(childId);
        userGroup.setUserId("user2");
        groupDao.addMember(userGroup);

        // 递归查询用户所在的群组（包括父群组）
        GroupCriteria criteria = new GroupCriteria()
                .setUserId("user2")
                .setRecursive(true);
        List<GroupDto> result = groupDao.findByCriteria(criteria, null);

        // 验证返回父子群组
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(parentId)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(childId)));
    }

    @Test
    public void testFindByCriteria_UserGroupLinkExists() {
        // 创建群组并关联用户
        Group group = new Group();
        String groupId = groupDao.create(group, "user1");
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(groupId);
        userGroup.setUserId("user2");
        groupDao.addMember(userGroup);

        // 查询并验证 userGroupDtoList 填充
        GroupCriteria criteria = new GroupCriteria()
                .setUserId("user2");
        List<GroupDto> result = groupDao.findByCriteria(criteria, null);

        // 确保 userGroupDtoList 不为空
        assertFalse(result.isEmpty());
    }

}