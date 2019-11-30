<!--修改用户的角色及用户组-->
<template>
  <div>
    <Row>
      <i-col span="6">
        <div class="margin-top-10">
          <Card :bordered="false" :dis-hover="true">
            <p slot="title">
              <Icon type="navicon-round"></Icon>
              用户组
            </p>
            <div class="edit-selector">
              <CheckboxGroup v-model="editData.group">
                <p v-for="item in allgroups" :key="item.id">
                  <Checkbox :label="item.id">{{ item.name }}</Checkbox>
                </p>
              </CheckboxGroup>
            </div>
          </Card>
        </div>
      </i-col>
      <i-col span="12">
        <Card :bordered="false" :dis-hover="true">
          <Form :label-width="100" inline>
            <!--<FormItem label="id" class="edit-form-item">-->
            <!--<div class="preview-content-con" v-html="editData.id"></div>-->
            <!--</FormItem>-->
            <FormItem label="登陆账号" class="edit-form-item">
              <Input v-model="editData.userName"/>
            </FormItem>
            <FormItem label="注册日期" class="edit-form-item">
              <DatePicker type="date" v-model="editData.regDate" readonly>
              </DatePicker>
            </FormItem>
            <FormItem label="出生日期" class="edit-form-item">
              <DatePicker type="date" v-model="editData.birthday">
              </DatePicker>
            </FormItem>
            <FormItem label="性别" class="edit-form-item">
              <RadioGroup v-model="editData.gender">
                <Radio label="男"></Radio>
                <Radio label="女"></Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="是否有效" class="edit-form-item">
              <i-switch v-model="editData.valid" size="large">
                <span slot="open">有效</span>
                <span slot="close">无效</span>
              </i-switch>
            </FormItem>
            <FormItem label="个人简介" class="edit-form-item">
              <Input v-model="editData.profile" type="textarea" :autosize="{minRows: 4}"
                     placeholder="请写下个人简介"/>
            </FormItem>
          </Form>
        </Card>
      </i-col>
      <i-col span="6">
        <div class="margin-top-10">
          <Card :bordered="false" :dis-hover="true">
            <Button type="primary" @click="freshEditor">刷新</Button>
            <Button v-if="editablee" type="success" @click="handlePublish">提交修改</Button>
            <Button type="dashed" @click="closeEditor">关闭</Button>
          </Card>
          <Card :bordered="false" :dis-hover="true">
            <p slot="title">
              <Icon type="navicon-round"></Icon>
              用户角色
            </p>
            <div class="edit-selector">
              <CheckboxGroup v-model="editData.roles">
                <p v-for="item in allroles" :key="item.id">
                  <Checkbox :label="item.id">{{ item.name }}</Checkbox>
                </p>
              </CheckboxGroup>
            </div>
          </Card>
        </div>
      </i-col>
    </Row>
  </div>
</template>
<script>
  import {mapGetters} from "vuex";
  import {formatDate, formatFullDate} from "@/libs/filters";
  import ISwitch from "iview/src/components/switch/switch";

  export default {
    name: "user-update-editor",
    components: {ISwitch},
    props: ["srcdata", "closemodal", "editablee"],
    data() {
      return {
        fullSrc: {},
        lastUserEditId: "",
        userGroups: "",
        userRoles: ""
      };
    },
    computed: {
      editData() {
        if (this.srcdata && this.srcdata.id && this.srcdata.id !== this.lastUserEditId) {
          this.$http.request({
            url: "/user/edit/get",
            params: {id: this.srcdata.id},
            method: "get"
          }).then(res => {
            this.fullSrc = res;
            this.fullSrc.regDate = formatFullDate(this.fullSrc.regDate);
            this.fullSrc.birthday = this.fullSrc.birthday ? formatDate(this.fullSrc.birthday) : "";
            this.lastUserEditId = this.srcdata.id;
          }).catch(err => this.$Notice.error({
            title: "获取用户信息失败",
            desc: err.msg
          }));
        }
        return this.fullSrc;
      },
      ...mapGetters(["allroles", "allmodules", "allgroups"])
    },
    methods: {
      freshEditor() {
        this.lastUserEditId = "";
        this.editData;
      },
      closeEditor() {
        this.$emit("update:closemodal", false);
      },
      findCategoryParents(theObject, item) {
        if (item && item.parent && item.parent !== "root") {
          theObject[item.parent] = item.parent;
          this.findCategoryParents(this.articleCategoryMap[item.parent]);
        }
      },
      setCategory(selected) {
        let nodes = {}, arr = [];
        selected.map(item => {
          this.findCategoryParents(nodes, item);
          nodes[item.id] = item.id;
        });
        for (let i in nodes) {
          arr.push(nodes[i]);
        }
        this.editData.categorys = arr;
      },
      handlePublish() {
        this.$http.request({
          url: "/user/edit/update",
          data: this.fullSrc,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改用户信息成功",
            duration: 1
          });
          this.endPost();
          this.$emit("update:closemodal", false);
        }).catch(err => {
          this.$Notice.error({
            title: "修改用户信息失败",
            desc: err.msg
          });
        });
      },
      endPost() {
        //  更新用户组信息到store
        this.$store.dispatch("init_user_groups");
      }
    },
    created() {
    },
    mounted() {
    }
  };
</script>
<style lang="less" scoped>
  .edit-form-item {
    width: 350px;
  }

  .edit-selector {
    max-height: 600px;
    overflow: auto;
  }

  .edit-scroll {
    height: 500px;
  }

  .edit-selector p label {
    padding-top: 10px;
    font-weight: 500;
    font-size: 13px;
  }
</style>
