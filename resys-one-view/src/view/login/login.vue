<style lang="less">
  @import './login.less';
</style>

<template>
  <div class="login">
    <div class="login-con">
      <Card icon="log-in" title="欢迎登录" :bordered="false">
        <div class="form-con">
          <login-form @on-success-valid="handleSubmit"></login-form>
        </div>
      </Card>
    </div>
  </div>
</template>

<script>
  import LoginForm from "_c/login-form";
  import {mapActions} from "vuex";

  export default {
    components: {
      LoginForm
    },
    methods: {
      handleSubmit({userName, password}) {
        this.$store.dispatch("login", {
          userName: userName,
          password: password
        }).then(res => {
          let last = JSON.stringify(res); // 将JSON对象转化为JSON字符
          this.$Notice.success({
            title: "登陆成功",
            desc: "欢迎" + res.userName,
            duration: 2
          });
          this.$router.push({name: this.$config.homeName});
        }).catch(err => this.$Notice.error({
          title: "登陆失败",
          desc: err.msg,
          duration: 2
        }));
      }
    }
  };
</script>

<style>
</style>
