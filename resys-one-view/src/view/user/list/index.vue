<!--用户列表-->
<template>
  <div>
    <Row>
      <i-col span="19" style="padding-right: 12px;">
        <Card>
          <p slot="title">
            <Icon type="md-list"></Icon>
            用户列表
          </p>
          <Button slot="extra" type="success" shape="circle" @click="modal2 = true">
            <Icon type="md-add"></Icon>
            添加
          </Button>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新用户列表
          </Button>
          <div class="filterTool">
            <div class="toolBar">
              <ButtonGroup>
                <Button
                  @click="filterData.valid=null;getFresh()"
                  :type="filterData.valid===null?'primary':null"
                >全部
                </Button>
                <Button
                  @click="filterData.valid=true;getFresh()"
                  :type="filterData.valid===true?'primary':null"
                >有效用户
                </Button>
                <Button
                  @click="filterData.valid=false;getFresh()"
                  :type="filterData.valid===false?'primary':null"
                >失效用户
                </Button>
              </ButtonGroup>
            </div>
          </div>
          <Table highlight-row :columns="columnsList" :data="userDatas"></Table>
          <Page
            v-show="filterData.page.totalSize > 10"
            :total="filterData.page.totalSize"
            show-sizer
            @on-change="getPageData"
            @on-page-size-change="setPageSize"
            :current="filterData.page.pageNumber"
            class="pageTool"
            :page-size-opts="[10,20,50,100,200,500,1000]"
          ></Page>
        </Card>
      </i-col>
      <i-col span="5" style>
        <Card>
          <p slot="title">
            <Icon type="md-search"></Icon>
            条件查询
          </p>
          <Button slot="extra" type="primary" shape="circle" @click="searchClick">
            <Icon type="md-search"></Icon>
            查询
          </Button>
          <Form :label-width="60">
            <FormItem label="用户id">
              <Input v-model="searchData.id"/>
            </FormItem>
            <FormItem label="登录账号">
              <Input v-model="searchData.userName"/>
            </FormItem>
            <FormItem label="性别">
              <Select v-model="searchData.gender" clearable>
                <Option value="男">男</Option>
                <Option value="女">女</Option>
              </Select>
            </FormItem>
          </Form>
        </Card>
        <br>
        <Card :bordered="false" :dis-hover="true">
          <p slot="title">
            <Icon type="md-menu"></Icon>
            用户组
          </p>
          <!-- <Button type="success" shape="circle" @click="onlyAdmins" slot="extra">仅管理员</Button> -->
          <Button type="primary" shape="circle" @click="pickAllGroups" slot="extra">全选
          </Button>
          <div class="edit-selector">
            <CheckboxGroup v-model="userGroupPick">
              <p v-for="item in allgroups" :key="item.id">
                <Checkbox :label="item.id">{{ item.name }}</Checkbox>
              </p>
            </CheckboxGroup>
          </div>
        </Card>
      </i-col>
      <Modal v-model="editUserModal" width="1000" :closable="false">
        <userUpdateEditor
          :closemodal.sync="editUserModal"
          :srcdata="editUserData"
          :editablee="true"
        />
        <div slot="footer"></div>
      </Modal>
      <Modal v-model="modal2" title="添加" @on-ok="addData" width="450">
        <Form :model="addItemData" label-position="left" :label-width="110">
          <FormItem label="(必填) 登录账号">
            <Input
              v-model="addItemData.userName"
              placeholder="请填写登录账号"
              style="width: 200px"
            />
          </FormItem>
          <FormItem label="(必填) 登录密码">
            <Input
              v-model="addItemData.password"
              placeholder="请填写登录密码"
              type="password"
              style="width: 200px"
            />
          </FormItem>
          <FormItem label="性别">
            <RadioGroup v-model="addItemData.gender">
              <Radio label="男"></Radio>
              <Radio label="女"></Radio>
            </RadioGroup>
          </FormItem>
          <FormItem label="用户组">
            <div style="max-height: 200px;overflow:auto;">
              <CheckboxGroup v-model="addItemData.group">
                <p v-for="item in allgroups" :key="item.id">
                  <Checkbox :label="item.id">{{ item.name }}</Checkbox>
                </p>
              </CheckboxGroup>
            </div>
          </FormItem>
        </Form>
      </Modal>
    </Row>
  </div>
</template>

<script>
  import {formatDate} from "@/libs/filters";
  import {mapGetters} from "vuex";
  import userUpdateEditor from "../components/user-update-editor";

  export default {
    components: {userUpdateEditor},
    data() {
      return {
        editUserModal: false,
        editUserData: {
          id: "",
          roleId: "",
          roles: ""
        },
        columnsList: [{
          title: "序号",
          type: "index",
          align: "center",
          width: 60
        }, {
          title: "用户名",
          key: "userName"
        }, {
          title: "注册日期",
          align: "center",
          key: "regDate",
          render: (h, params) => {
            if (params.row.regDate) {
              return h("div", formatDate(params.row.regDate));
            } else {
              return h("div", "-");
            }
          }
        }, {
          title: "有效性",
          align: "center",
          key: "valid",
          width: 95,
          render: (h, params) => {
            if (params && params.row) {
              return params.row.valid
                ? h("Tag", {props: {color: "green"}}, "有效")
                : h("Tag", {props: {color: "red"}}, "失效");
            }
          }
        }, {
          title: "角色",
          align: "center",
          key: "roles",
          width: 120,
          render: (h, params) => {
            if (params.row.roles != null && params.row.roles.length > 0) {
              return params.row.roles.map(role => h("Tag", {
                props: {
                  type: "border",
                  size: "small"
                }
              }, role));
            } else {
              return h("div", "无");
            }
          }
        }, {
          title: "用户组",
          align: "center",
          key: "group",
          render: (h, params) => {
            if (params.row.group != null && params.row.group.length > 0) {
              return params.row.group.map(g => h("Tag", {
                props: {
                  type: "border",
                  size: "small"
                }
              }, g));
            } else {
              return h("div", "无");
            }
          }
        }, {
          title: "操作",
          align: "center",
          width: 80,
          key: "handle",
          render: (h, params) => {
            return h("div", [
              h(
                "Button",
                {
                  props: {
                    type: "primary",
                    size: "small"
                  },
                  style: {
                    marginRight: "5px"
                  },
                  on: {
                    click: () => {
                      this.handleEdit(params.row);
                    }
                  }
                },
                "编辑"
              )
            ]);
          }
        }],
        searchData: {
          id: null,
          userName: null
        },
        addItemData: {group: []}, //  定义添加数据
        modal2: false, //  定义mode2
        // userGroupPick: this.$store.getters.allgroups.filter(g => ['sim', 'tmp'].indexOf(g.id) === -1).map(g => g.id),
        userGroupPick: [],
        userDatas: [],
        filterData: {
          valid: true,
          page: {
            pageNumber: 1,
            totalSize: 10,
            pageSize: 10
          }
        }
      };
    },
    computed: {
      ...mapGetters(["allroles", "allmodules", "allgroups", "allrolesArray", "allgroupsArray"])
    },
    methods: {
      handleEdit(val) {
        this.editUserModal = true;
        this.editUserData = val;
      },
      searchClick() {
        for (let k in this.searchData) {
          let val = this.searchData[k];
          if (val) {
            this.filterData[k] = this.searchData[k];
          } else {
            this.filterData[k] = null;
          }
        }
        if (
          !(
            this.filterData.regDate &&
            this.filterData.regDate[0] &&
            this.filterData.regDate[1]
          )
        ) {
          this.filterData.regDate = null;
        }
        // this.searchDataSearching = true;
        this.getData();
      },
      pickAllGroups() {
        //  用户组全选及取消全选
        if (this.userGroupPick.length !== this.allgroups.length) {
          this.userGroupPick = this.allgroups.map(item => item.id);
        } else {
          this.userGroupPick = [];
        }
      },
      freshData() {
        this.filterData = {
          valid: true,
          page: {
            pageNumber: 1,
            totalSize: 0,
            pageSize: 10
          }
        };
        this.getData();
      },
      getFresh() {
        this.filterData.page = {
          pageNumber: 1,
          totalSize: 0,
          pageSize: 10
        };
        this.getData();
      },
      addData() {
        let obj = this.$data.addItemData;
        if (obj.userName == null && obj.password == null) {
          this.$Message.error("账号或密码不能为空!");
        } else {
          this.$http.request({
            url: "/user/edit",
            data: obj,
            method: "put"
          }).then(
            res => {
              this.$Notice.success({
                title: "添加成功"
              });
              //  成功后数据置为空
              this.addItemData = {group: []};
              this.getData();
            },
            error => {
              this.$Notice.error({
                title: "添加失败"
              });
            }
          );
        }
      },
      getData() {
        this.$http.request({
          url: "/user/edit/query",
          data: this.filterData,
          method: "post"
        }).then(res => {
          this.userDatas = res.list.map(item => {
            return item;
          });
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新用户数据",
            duration: 1
          });
        });
      },
      getPageData(val) {
        this.filterData.page.pageNumber = val;
        this.getData();
      },
      setPageSize(val) {
        this.filterData.page.pageSize = val;
        this.getData();
      }
    },
    watch: {
      userGroupPick: function (newval, oldval) {
        //  用户组的筛选
        if (newval.length > 0) {
          this.filterData.group = newval;
        } else {
          delete this.filterData.group;
        }
        this.getData();
      },
      editUserModal: function (newVal, oldVal) {
        if (newVal === false && oldVal === true) {
          this.freshData();
        }
      }
    },
    created() {
      // 查看store中是否请求过roles(角色)数据，没有的话请求数据
      if (!this.$store.state.user.getRoles) {
        this.$store.dispatch("init_user_roles");
      }
      // 查看store中是否请求过group（用户组）数据，没有的话请求数据
      if (!this.$store.state.user.getGroup) {
        this.$store.dispatch("init_user_groups");
      }
    },
    mounted() {
      this.getData();
    }
  };
</script>

<style lang="less" scoped>
  .edit-selector {
    min-height: 300px;
    max-height: 600px;
    overflow: auto;
  }

  .edit-selector p label {
    padding-top: 10px;
    font-weight: 600;
    font-size: 15px;
  }

</style>
