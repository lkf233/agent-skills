package org.lkf.agent.common.context;

import org.lkf.agent.common.exception.BusinessException;

/**
 * 用户上下文。
 */
public final class UserContext {

    /**
     * 当前线程用户信息。
     */
    private static final ThreadLocal<CurrentUser> CURRENT_USER_HOLDER = new ThreadLocal<>();

    /**
     * 私有构造器。
     */
    private UserContext() {
    }

    /**
     * 设置当前用户信息。
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public static void setCurrentUser(Long userId, String username) {
        CURRENT_USER_HOLDER.set(new CurrentUser(userId, username));
    }

    /**
     * 获取当前用户信息。
     *
     * @return 当前用户信息，可能为null
     */
    public static CurrentUser getCurrentUser() {
        return CURRENT_USER_HOLDER.get();
    }

    /**
     * 获取当前用户名（必须存在）。
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        CurrentUser currentUser = CURRENT_USER_HOLDER.get();
        if (currentUser == null || currentUser.getUsername() == null) {
            throw new BusinessException(401, "未获取到当前用户信息");
        }
        return currentUser.getUsername();
    }

    /**
     * 获取当前用户ID（必须存在）。
     *
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        CurrentUser currentUser = CURRENT_USER_HOLDER.get();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(401, "未获取到当前用户信息");
        }
        return currentUser.getUserId();
    }

    /**
     * 清理当前线程用户信息。
     */
    public static void clear() {
        CURRENT_USER_HOLDER.remove();
    }

    /**
     * 当前用户信息对象。
     */
    public static class CurrentUser {

        /**
         * 用户ID。
         */
        private final Long userId;

        /**
         * 用户名。
         */
        private final String username;

        /**
         * 构造器。
         *
         * @param userId 用户ID
         * @param username 用户名
         */
        public CurrentUser(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        /**
         * 获取用户ID。
         *
         * @return 用户ID
         */
        public Long getUserId() {
            return userId;
        }

        /**
         * 获取用户名。
         *
         * @return 用户名
         */
        public String getUsername() {
            return username;
        }
    }
}
