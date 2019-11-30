<template>
  <Row>
    <i-col class="margin-top-10">
      <i-col span="24">
        <Card>
          <span slot="title">
            <Icon type="md-list"></Icon>
            用户组列表
          </span>
          <i-input slot="title" v-model="filterData.id" style="width: 200px;margin-left: 5px"
                   placeholder="组编号"></i-input>
          <i-input slot="title" v-model="filterData.name" style="width: 200px;margin-left: 5px"
                   placeholder="组名称"></i-input>
          <Button slot="title" type="primary" icon="md-search" style="margin-left: 5px"
                  @click="freshData">查询
          </Button>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新
          </Button>
          <Button slot="extra" type="success" shape="circle" @click="modal2 = true">
            <Icon type="md-add"></Icon>
            添加
          </Button>
          <div>
            <Table highlight-row :columns="mainColumns" :data="mainData"></Table>
            <Page :total="filterData.page.totalSize" @on-change="getPageData"
                  @on-page-size-change="setPageSize" class="pageTool" show-sizer
                  :page-size-opts="[10,20,50,100,200,500,1000]"></Page>
          </div>
        </Card>
      </i-col>
    </i-col>
    <Modal v-model="modal1" title="编辑" @on-ok="updateEditor">
      <Form :model="editmainData" label-position="left" :label-width="100">
        <FormItem label="编号">
          <i-input disabled v-model="editmainData.id"/>
        </FormItem>
        <FormItem label="名称">
          <i-input v-model="editmainData.name"/>
        </FormItem>
        <FormItem label="组长">
          <i-input v-model="editmainData.leader.userName"/>
        </FormItem>
        <FormItem label="该用户组的角色">
          <CheckboxGroup v-model="editmainData.roles">
            <p v-for="item in allroles" :key="item.id">
              <Checkbox :label="item.id">{{ item.name }}</Checkbox>
            </p>
          </CheckboxGroup>
        </FormItem>
        <FormItem label="作用">
          <i-input v-model="editmainData.feature"/>
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="modal2" title="添加" @on-ok="addEditor">
      <Form :model="addData" label-position="left" :label-width="100">
        <FormItem label="编号">
          <i-input v-model="addData.id"/>
        </FormItem>
        <FormItem label="名称">
          <i-input v-model="addData.name"/>
        </FormItem>
        <FormItem label="组长">
          <i-input v-model="addData.leader.userName"/>
        </FormItem>
        <FormItem label="该用户组的角色">
          <CheckboxGroup v-model="addData.roles">
            <p v-for="item in allroles" :key="item.id">
              <Checkbox :label="item.id">{{ item.name }}</Checkbox>
            </p>
          </CheckboxGroup>
        </FormItem>
        <FormItem label="作用描述">
          <i-input v-model="addData.feature"/>
        </FormItem>
      </Form>
    </Modal>
  </Row>
</template>

<script>
  import {mapGetters} from "vuex";
  import {formatDate} from "@/libs/filters";

  export default {
    components: {},
    data() {
      return {
        dataName: "usergroup",
        modal1: false,
        modal2: false,
        mainColumns: [{
          title: "编号",
          key: "id"
        }, {
          title: "组名",
          key: "name"
        }, {
          title: "组长",
          key: "leader",
          render: (h, params) => {
            if (params && params.row) {
              return h("span", params.row.leader.userName);
            }
          }
        }, {
          title: "包含角色",
          key: "roles",
          render: (h, params) => {
            if (params.row.roles && params.row.roles.length > 0) {
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
          title: "作用",
          key: "feature"
        }, {
          title: "操作",
          key: "action",
          align: "center",
          width: 140,
          render: (h, params) => {
            return h("div", [
              h("Button", {
                props: {
                  type: "success",
                  size: "small"
                },
                style: {
                  marginRight: "6px"
                },
                on: {
                  click: () => {
                    this.modal1 = true;
                    this.$data.editmainData = params.row;
                  }
                }
              }, "编辑"),
              h("Button", {
                props: {
                  type: "error",
                  size: "small"
                },
                style: {
                  marginRight: "6px"
                },
                on: {
                  click: () => {
                    this.$Modal.confirm({
                      title: "删除确认",
                      content: "<p>是否确认删除用户组【<string>" + params.row.id + "</strong>】？</p><p>删除操作不可恢复！</p>",
                      onOk: () => {
                        this.deleteGroup(params.row.id);
                      }
                    });
                    this.$data.editmainData = params.row;
                  }
                }
              }, "删除")
            ]);
          }
        }],
        mainData: [],
        editmainData: {
          leader: {userName: ""}
        },
        addData: {
          leader: {userName: ""}
        },
        filterData: {
          page: {
            pageNumber: 1,
            totalSize: 0,
            pageSize: 10
          }
        }
      };
    },
    computed: {
      groupPeople() {
        return this.editmainData.id && this.groupMap && this.groupMap[this.editmainData.id] ? this.groupMap[this.editmainData.id].users : [];
      },
      ...mapGetters([
        "allroles",
        "allmodules",
        "groupMap"
      ])
    },
    methods: {
      getData() {
        this.$http.request({
          url: "/" + this.$data.dataName + "/query",
          data: this.$data.filterData,
          method: "post"
        }).then(res => {
          this.mainData = res.list.map((val) => {
            val.leader = val.leader ? val.leader : {id: ""};
            val.viceLeaders = val.viceLeaders ? val.viceLeaders : [];
            val.viceLeaderNames = val.viceLeaders.map(item => item.id);
            return val;
          });
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新数据"
          });
        });
      },
      deleteGroup(groupId) {
        this.$http.request({
          url: "/" + this.$data.dataName + "/remove",
          params: {id: groupId},
          method: "get"
        }).then(() => {
          this.$Notice.success({
            title: "删除成功",
            duration: 1
          });
          this.endEdit();
        });
      },
      updateEditor() {
        if (this.$data.editmainData) {
          this.$http.request({
            url: "/" + this.$data.dataName + "/update",
            data: this.$data.editmainData,
            method: "post"
          }).then(() => {
            this.$Notice.success({
              title: "修改成功"
            });
            this.endEdit();
          }).catch(err => {
            this.$Notice.error({
              title: "修改失败！",
              desc: err.response.data.errMsg,
              duration: 2
            });
          });
        }
      },
      addEditor() {
        this.$http.request({
          url: "/" + this.$data.dataName,
          data: this.$data.addData,
          method: "put"
        }).then(() => {
          this.$Notice.success({
            title: "添加成功"
          });
          this.addData = {
            leader: {}
          };
          this.endEdit();
        }).catch(err => {
          this.$Notice.error({
            title: "添加失败！",
            desc: err.response.data.errMsg,
            duration: 2
          });
        });
      },
      endEdit() {
        this.$store.dispatch("init_user_groups");
        this.freshData();
      },
      freshData() {
        this.getData();
      },
      getPageData(val) {
        this.$data.filterData.page.pageNumber = val;
        this.getData();
      },
      setPageSize(val) {
        this.$data.filterData.page.pageSize = val;
        this.getData();
      }
    },
    created() {
      // 查看store中是否请求过roles(角色)数据，没有的话请求数据
      if (!this.$store.state.user.getRoles) {
        this.$store.dispatch("init_user_roles");
      }
      //  groupMap 依赖 group
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
</style>
