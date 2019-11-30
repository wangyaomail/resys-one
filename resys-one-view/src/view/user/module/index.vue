<!--系统模块管理-->
<template>
  <Row>
    <i-col class="margin-top-10">
      <i-col span="24">
        <Card>
          <span slot="title">
            <Icon type="md-list"></Icon>
            模块列表
          </span>
          <i-input slot="title" v-model="filterData.id" style="width: 200px;margin-left: 5px"
                   placeholder="模块编号"></i-input>
          <i-input slot="title" v-model="filterData.name" style="width: 200px;margin-left: 5px"
                   placeholder="模块名称"></i-input>
          <Button slot="title" type="primary" icon="md-search" style="margin-left: 5px"
                  @click="freshData">查询
          </Button>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新
          </Button>
          <Button slot="extra" type="success" shape="circle" @click="modal2 = true">
            <Icon type="plus"></Icon>
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
      <Modal v-model="modal1" title="修改" @on-ok="updateEditor" width="800">
        <Form :model="editmainData" label-position="left" :label-width="100">
          <FormItem label="编号">
            <i-input disabled type="text" v-model="editmainData.id"/>
          </FormItem>
          <FormItem label="名称">
            <i-input type="text" v-model="editmainData.name"/>
          </FormItem>
          <FormItem label="记录访问日志">
            <i-switch v-model="editmainData.record" size="large">
              <span slot="open">记录</span>
              <span slot="close">不记</span>
            </i-switch>
          </FormItem>
          <FormItem label="过滤规则">
            <Row type="flex" justify="center">
              <i-col span="2">
                <i-input disabled v-model="editTempData._index" placeholder="序号"/>
              </i-col>
              <i-col span="4">
                <Select v-model="editTempData.method">
                  <Option v-for="item in methodList" :value="item" :key="item">{{ item }}
                  </Option>
                </Select>
              </i-col>
              <i-col span="12">
                <i-input v-model="editTempData.url" placeholder="url"/>
              </i-col>
              <i-col span="2">
                <Button type="success" @click="addTempToUrl">新增</Button>
              </i-col>
              <i-col span="2">
                <Button type="primary" @click="updateTempToUrl">修改</Button>
              </i-col>
            </Row>
            <Table :columns="editColomn" :data="editmainData.urls" size="small"></Table>
          </FormItem>
          <FormItem label="模块说明">
            <i-input type="textarea" :autosize="{minRows: 2,maxRows: 5}"
                     v-model="editmainData.feature"/>
          </FormItem>
        </Form>
      </Modal>
      <Modal v-model="modal2" title="添加" @on-ok="addEditor" width="800">
        <Form :model="editmainData" label-position="left" :label-width="100">
          <FormItem label="编号">
            <i-input v-model="addData.id"/>
          </FormItem>
          <FormItem label="名称">
            <i-input v-model="addData.name"/>
          </FormItem>
          <FormItem label="是否记录访问日志">
            <i-switch v-model="addData.record" size="large">
              <span slot="open">记录</span>
              <span slot="close">不记</span>
            </i-switch>
          </FormItem>
          <FormItem label="过滤规则">
            <Row type="flex" justify="center">
              <i-col span="2">
                <i-input disabled v-model="addTempData._index" placeholder="序号"/>
              </i-col>
              <i-col span="4">
                <Select v-model="addTempData.method">
                  <Option v-for="item in methodList" :value="item" :key="item">{{ item }}
                  </Option>
                </Select>
              </i-col>
              <i-col span="12">
                <i-input v-model="addTempData.url" placeholder="url"/>
              </i-col>
              <i-col span="2">
                <Button type="success" @click="addTempToUrlByAdd">新增</Button>
              </i-col>
              <i-col span="2">
                <Button type="primary" @click="updateTempToUrlByAdd">修改</Button>
              </i-col>
            </Row>
            <Table :columns="addColomn" :data="addData.urls" size="small"></Table>
          </FormItem>
          <FormItem label="模块说明">
            <i-input type="textarea" :autosize="{minRows: 2,maxRows: 5}"
                     v-model="addData.feature"/>
          </FormItem>
        </Form>
      </Modal>
    </i-col>
  </Row>
</template>

<script>
  import {formatDate} from "@/libs/filters";

  export default {
    components: {},
    data() {
      return {
        dataName: "module",
        modal1: false,
        modal2: false,
        mainColumns: [{
          title: "编号",
          key: "id",
          width: 120
        }, {
          title: "名称",
          key: "name",
          width: 120
        }, {
          title: "urls",
          key: "urls",
          render: (h, params) => {
            if (params.row.urls) {
              return params.row.urls.map(item => {
                let itemShowVal = item.method;
                let propcolor = "default";
                switch (item.method) {
                  case "ALL":
                    propcolor = "warning";
                    break;
                  case "PUT":
                  case "POST":
                    propcolor = "primary";
                    break;
                  case "GET":
                    propcolor = "success";
                    break;
                  case "DELETE":
                    propcolor = "error";
                    break;
                  default:
                    propcolor = "success";
                    break;
                }
                return h("p", [h("Tag", {
                  props: {color: propcolor},
                  style: {fontSize: "14px", minWidth: "70px"}
                }, itemShowVal), h("span", {
                  style: {
                    fontSize: "14px",
                    fontFamily: "Consolas Verdana",
                    marginLeft: "5px"
                  }
                }, item.url)]);
              });
            }
          }
        }, {
          title: "记录",
          key: "record",
          width: 80,
          render: (h, params) => {
            return params.row.record
              ? h("Tag", {props: {color: "success"}}, "记录")
              : h("Tag", {props: {color: "error"}}, "不记");
          }
        }, {
          title: "功能",
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
                  type: "warning",
                  size: "small"
                },
                style: {
                  marginRight: "6px"
                },
                on: {
                  click: () => {
                    this.modal1 = true;
                    this.editmainData = JSON.parse(JSON.stringify(params.row));
                  }
                }
              }, "修改"),
              h("Button", {
                props: {
                  type: "error",
                  size: "small"
                },
                on: {
                  click: () => {
                    this.$Modal.confirm({
                      title: "删除确认",
                      content: "<p>是否确认删除模块【<string>" + params.row.id + "</strong>】？</p><p>删除操作不可恢复！</p>",
                      onOk: () => {
                        this.deleteModule(params.row.id);
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
        editTempData: {
          method: "",
          url: ""
        },
        methodList: [
          "ALL",
          "PUT",
          "GET",
          "POST",
          "DELETE"
        ],
        editColomn: [{
          type: "index",
          width: 60,
          align: "center"
        }, {
          title: "method",
          key: "method",
          width: 100
        }, {
          title: "url",
          key: "url"
        }, {
          title: "操作",
          key: "action",
          align: "center",
          width: 140,
          render: (h, params) => {
            return h("div", [
              h("Button", {
                props: {
                  type: "warning",
                  size: "small"
                },
                style: {
                  marginRight: "6px"
                },
                on: {
                  click: () => {
                    this.editTempData = JSON.parse(JSON.stringify(params.row));
                  }
                }
              }, "修改"),
              h("Button", {
                props: {
                  type: "error",
                  size: "small"
                },
                on: {
                  click: () => {
                    this.editmainData.urls.splice(params.row._index, 1);
                  }
                }
              }, "删除")
            ]);
          }
        }],
        addColomn: [{
          type: "index",
          width: 60,
          align: "center"
        }, {
          title: "method",
          key: "method",
          width: 100
        }, {
          title: "url",
          key: "url"
        }, {
          title: "操作",
          key: "action",
          align: "center",
          width: 140,
          render: (h, params) => {
            return h("div", [
              h("Button", {
                props: {
                  type: "warning",
                  size: "small"
                },
                style: {
                  marginRight: "6px"
                },
                on: {
                  click: () => {
                    this.addTempData = JSON.parse(JSON.stringify(params.row));
                  }
                }
              }, "修改"),
              h("Button", {
                props: {
                  type: "error",
                  size: "small"
                },
                on: {
                  click: () => {
                    this.addData.urls.splice(params.row._index, 1);
                  }
                }
              }, "删除")
            ]);
          }
        }],
        addData: {
          urls: []
        },
        addTempData: {
          method: "",
          url: ""
        },
        filterData: {
          page: {
            pageNumber: 1,
            totalSize: 0,
            pageSize: 10
          }
        },
        searchName: ""
      };
    },
    computed: {},
    methods: {
      getData() {
        this.$http.request({
          url: "/" + this.$data.dataName + "/query",
          data: this.$data.filterData,
          method: "post"
        }).then(res => {
          this.mainData = res.list.map((val) => {
            if (val.urls) {
              val.urlLink = JSON.stringify(val.urls);
            }
            return val;
          });
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新数据"
          });
        });
      },
      deleteModule(moduleId) {
        this.$http.request({
          url: "/" + this.$data.dataName + "/remove",
          params: {id: moduleId},
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
        this.$http.request({
          url: "/" + this.$data.dataName + "/update",
          data: this.$data.editmainData,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改成功"
          });
          this.updateStore();
          this.freshData();
        });
      },
      addEditor() {
        this.$http.request({
          url: "/" + this.$data.dataName,
          data: this.$data.addData,
          method: "put"
        }).then((res) => {
          if (res) {
            this.$Notice.success({
              title: "添加成功"
            });
            this.addData = {
              record: false,
              urls: []
            };
            this.updateStore();
            this.freshData();
          } else {
            this.$Notice.error({
              title: "添加失败"
            });
          }
        });
      },
      freshData() {
        this.getData();
      },
      updateStore() {
        //  更新系统模块数据到store
        this.$store.dispatch("init_user_modules");
      },
      addTempToUrl() {
        this.editmainData.urls = this.editmainData.urls ? this.editmainData.urls : [];
        this.editmainData.urls.push(JSON.parse(JSON.stringify(this.editTempData)));
      },
      updateTempToUrl() {
        this.editmainData.urls.splice(this.editTempData._index, 1, JSON.parse(JSON.stringify(this.editTempData)));
      },
      addTempToUrlByAdd() {
        this.addData.urls = this.addData.urls ? this.addData.urls : [];
        this.addData.urls.push(JSON.parse(JSON.stringify(this.addTempData)));
      },
      updateTempToUrlByAdd() {
        this.addData.urls.splice(this.addTempData._index, 1, JSON.parse(JSON.stringify(this.addTempData)));
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
    },
    mounted() {
      this.getData();
    }
  };
</script>

<style lang="less" scoped>
</style>
