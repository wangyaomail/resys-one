<template>
  <div>
    <Row>
      <Card>
        <p slot="title">
          <Icon type="md-paper"></Icon>
          任务列表
        </p>
        <Button slot="extra" type="primary" shape="circle" @click="freshData">
          <Icon type="md-refresh"></Icon>
          刷新列表
        </Button>
        <div style="padding-bottom: 35px">
          <Table highlight-row :columns="columnsList" :data="tableData"></Table>
          <Page :total="filterData.page.totalSize" :current="filterData.page.pageNumber"
                show-sizer @on-change="getPageData" @on-page-size-change="setPageSize"
                class="pageTool" :page-size-opts="[15,30,100,200,500,1000]"
                style="padding-top: 5px;float:right"></Page>
        </div>
      </Card>
    </Row>
  </div>
</template>

<script>
  import {formatDate} from "@/libs/filters";

  export default {
    components: {},
    data() {
      return {
        columnsList: [{
          title: "编号",
          key: "id",
          width: 100
        }, {
          title: "command",
          key: "command"
        }, {
          title: "执行周期",
          key: "period",
          width: 100
        }, {
          title: "是否有效",
          key: "valid",
          width: 60
        }, {
          title: "操作",
          align: "center",
          key: "handle",
          width: 135,
          render: (h, params) => {
            return h("div", [
              h(
                "Button",
                {
                  props: {
                    size: "small"
                  },
                  style: {
                    marginRight: "5px"
                  },
                  on: {
                    click: () => {
                      this.editData = params.row;
                      this.editArticleState = true;
                    }
                  }
                },
                this.articleStateMapRevert[params.row.state].name
              ),
              h("Button", {
                  props: {type: "success", size: "small"},
                  on: {
                    click: () => {
                      this.editArticleId = params.row.id;
                      this.editModal = true;
                    }
                  }
                },
                "修改"),
              h("Button", {
                props: {type: "warning", size: "small"},
                style: {marginRight: "5px"},
                on: {
                  click: () => {
                    this.handleFakeDel(params.row.id);
                  }
                }
              }, "回收"),
              h("Button", {
                props: {type: "error", size: "small"},
                on: {
                  click: () => {
                    this.handleRealDel(params.row.id);
                  }
                }
              }, "删除")
            ]);
          }
        }],
        tableData: [],
        editModal: false,
        editData: {},
        filterData: {
          page: {
            pageNumber: 1,
            totalSize: 0,
            pageSize: 10
          }
        }
      };
    },
    computed: {},
    methods: {
      freshData() {
        this.filterData.page = {
          pageNumber: 1,
          totalSize: 0,
          pageSize: 10
        };
        this.getData();
      },
      handleFakeDel(val) {
        this.$Modal.confirm({
          title: "确定要回收吗？",
          content: "<p>已删除的文章可以在回收站中找到</p>",
          onOk: () => {
            this.$http.request({
              url: "/article/fakedel",
              params: {id: val},
              method: "get"
            }).then(() => {
              this.getFresh();
              this.$Notice.success({
                title: "文章已移动到回收站",
                duration: 1
              });
            });
          }
        });
      },
      handleRealDel(val) {
        this.$Modal.confirm({
          title: "确定要删除吗？",
          content: "<p>此操作会彻底删除该文章</p>",
          onOk: () => {
            this.$http.request({
              url: "/article/remove",
              params: {id: val},
              method: "get"
            }).then(() => {
              this.getFresh();
              this.$Notice.success({
                title: "删除文章成功",
                duration: 1
              });
            });
          }
        });
      },
      submitEditArticle() {
        this.$http.request({
          url: "/article/update",
          data: this.editData,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改文章成功",
            duration: 1
          });
        });
      },
      getData() {
        this.filterData.page = {
          pageNumber: 1,
          totalSize: 0,
          pageSize: 15
        };
        this.getFresh();
      },
      getFresh() {
        this.$http.request({
          url: "/pipeline/query",
          data: this.$data.filterData,
          method: "post"
        }).then(res => {
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新数据",
            duration: 1
          });
        });
      },
      getPageData(val) {
        this.filterData.page.pageNumber = val;
        this.getFresh();
      },
      setPageSize(val) {
        this.filterData.page.pageSize = val;
        this.getFresh();
      }
    },
    mounted() {
      this.getData();
    },
    destroyed() {
    }
  };
</script>
<style lang="less">
</style>
