<!--角色管理-->
<template>
  <Row>
    <i-col class="margin-top-10">
      <i-col span="24">
        <Card>
          <span slot="title">
            <Icon type="md-list"></Icon>
            角色列表
          </span>
          <i-input slot="title" v-model="filterData.id" style="width: 200px;margin-left: 5px"
                   placeholder="角色编号"></i-input>
          <i-input slot="title" v-model="filterData.name" style="width: 200px;margin-left: 5px"
                   placeholder="角色名称"></i-input>
          <Button slot="title" type="primary" icon="md-search" style="margin-left: 5px"
                  @click="freshData">查询
          </Button>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新
          </Button>
          <Button slot="extra" type="success" shape="circle" @click="modal2= true">
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
        <FormItem label="编号（id）">
          <i-input disabled v-model="editmainData.id"></i-input>
        </FormItem>
        <FormItem label="名称（show）">
          <i-input v-model="editmainData.name"></i-input>
        </FormItem>
        <FormItem label="权限（module）">
          <CheckboxGroup v-model="editmainData.modules">
            <Checkbox v-for="mod in allmodules" :label="mod.id" :key="mod.id"></Checkbox>
          </CheckboxGroup>
        </FormItem>
        <FormItem label="硬权限">
          <i-input v-model="editmainData.jwt"></i-input>
          硬权限用来做第一重接口过滤，过滤速度快
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="modal2" title="添加" @on-ok="addEditor">
      <Form :model="addData" label-position="left" :label-width="100">
        <FormItem label="编号（id）">
          <i-input v-model="addData.id"></i-input>
        </FormItem>
        <FormItem label="名称（show）">
          <i-input v-model="addData.name"></i-input>
        </FormItem>
        <FormItem label="权限（module）">
          <CheckboxGroup v-model="addData.modules">
            <Checkbox v-for="mod in allmodules" :label="mod.id" :key="mod.id"></Checkbox>
          </CheckboxGroup>
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
        dataName: "role",
        modal1: false,
        modal2: false,
        mainColumns: [{
          title: "编号",
          key: "id"
        }, {
          title: "名称",
          key: "name"
        }, {
          title: "硬权限",
          key: "jwt"
        }, {
          title: "包含模块",
          key: "modules",
          render: (h, params) => {
            if (params.row.modules && params.row.modules.length > 0) {
              return params.row.modules.map(role => h("Tag", {
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
                      content: "<p>是否确认删除角色【<string>" + params.row.id + "</strong>】？</p><p>删除操作不可恢复！</p>",
                      onOk: () => {
                        this.deleteRole(params.row.id);
                      }
                    });
                  }
                }
              }, "删除")
            ]);
          }
        }],
        mainData: [],
        editmainData: {},
        addData: {},
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
      ...mapGetters([
        "allmodules"
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
            return val;
          });
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新数据",
            duration: 1
          });
        });
      },
      deleteRole(roleId) {
        this.$http.request({
          url: "/" + this.$data.dataName + "/remove",
          params: {id: roleId},
          method: "get"
        }).then(() => {
          this.$Notice.success({
            title: "删除成功",
            duration: 1
          });
          this.freshData();
          this.updateStore();
        });
      },
      updateEditor() {
        let updateObj = JSON.parse(JSON.stringify(this.$data.editmainData));
        console.log(this.$refs.tree.getCheckedNodes());
        let checkedNodes = this.$refs.tree.getCheckedNodes();
        let modules = [];
        for (let i in checkedNodes) {
          if (!checkedNodes[i].hasOwnProperty("children")) {
            modules.push(checkedNodes[i].id);
          }
        }
        updateObj.modules = modules;
        this.$http.request({
          url: "/" + this.$data.dataName + "/update",
          data: updateObj,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改成功",
            duration: 1
          });
          this.freshData();
          this.updateStore();
        });
      },
      addEditor() {
        this.$http.request({
          url: "/" + this.$data.dataName,
          data: this.$data.addData,
          method: "put"
        }).then(() => {
          this.$Notice.success({
            title: "添加成功",
            duration: 1
          });
          this.addData = {};
          this.freshData();
          this.updateStore();
        });
      },
      freshData() {
        this.getData();
      },
      updateStore() {
        //  更新角色数据到store
        this.$store.dispatch("init_user_roles");
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
      // 查看store中是否请求过modules数据，没有的话请求数据
      if (!this.$store.state.user.getModules) {
        this.$store.dispatch("init_user_modules");
      }
    },
    mounted() {
      this.getData();
    }
  };
</script>

<style lang="less" scoped>
</style>
