<template>
  <div class="user-avatar-dropdown">
    <Dropdown @on-click="handleClick">
      <Avatar icon="ios-person" :src="userAvatar"/>
      <Icon :size="18" type="md-arrow-dropdown"></Icon>
      <DropdownMenu slot="list">
        <DropdownItem name="logout">退出登录</DropdownItem>
      </DropdownMenu>
    </Dropdown>
  </div>
</template>

<script>
  import "./user.less";
  import {mapActions} from "vuex";

  export default {
    name: "User",
    props: {
      userAvatar: {
        type: String,
        default: ""
      },
      messageUnreadCount: {
        type: Number,
        default: 0
      }
    },
    methods: {
      logout() {
        // 退出登录
        this.$store.dispatch('logout', this);
        this.$Notice.success({
          title: "退出登陆成功",
          duration: 2
        });
        this.$router.push({
          name: 'login'
        });
      },
      handleClick(name) {
        switch (name) {
          case "logout":
            this.logout();
            break;
        }
      }
    }
  };
</script>
