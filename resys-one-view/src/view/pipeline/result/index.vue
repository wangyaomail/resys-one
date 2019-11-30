<template>
  <div>
    <Row>
      <i-col span="19" style="padding-right: 12px;">
        <Card>
          <p slot="title">
            <Icon type="md-paper"></Icon>
            执行结果
          </p>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新列表
          </Button>
          <div style="padding-bottom: 35px">
            <Table highlight-row :columns="columnsList" :data="articleData"></Table>
            <Page :total="filterData.page.totalSize" :current="filterData.page.pageNumber"
                  show-sizer @on-change="getPageData" @on-page-size-change="setPageSize"
                  class="pageTool" :page-size-opts="[15,30,100,200,500,1000]"
                  style="padding-top: 5px;float:right"></Page>
          </div>
        </Card>
      </i-col>
      <i-col span="5" style="">
        <Card>
          <p slot="title">
            <Icon type="md-list"></Icon>
            文章分类
          </p>
          <Button type="primary" shape="circle" @click="pickAllCategory" slot="extra">全选</Button>
          <div class="classification-con">
            <Tree ref="catetree" :data="articleCategory" @on-check-change="setCategory"
                  check-directly show-checkbox></Tree>
          </div>
        </Card>
      </i-col>
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
          title: "标题",
          key: "title"
        }, {
          title: "创建时间 / 发文时间",
          key: "createTime",
          width: 100,
          render: (h, params) => {
            let showTime = "";
            showTime += params.row.createTime ? formatDate(params.row.createTime) : "-";
            showTime += " / ";
            showTime += params.row.publishTime ? formatDate(params.row.publishTime) : "-";
            return h("div", showTime);
          }
        }, {
          title: "文章作者",
          key: "articleAuthor",
          width: 60
        }, {
          title: "分类",
          align: "center",
          key: "categorys",
          width: 140,
          render: (h, params) => {
            if (params.row.categorys && params.row.categorys.length > 0) {
              return h("div", params.row.categorys.map(item => {
                return h("Tag", {props: {color: "geekblue"}, style: {marginRight: "2px"}}, item);
              }));
            } else {
              return h("div", "无");
            }
          }
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
                      this.editArticleData = params.row;
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
                      this.editArticleModal = true;
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
        articleData: [],
        editArticleModal: false,
        editArticleData: {
          author: {}
        },
        editArticleId: "",
        articleCategory: [],
        editArticleState: false,
        filterData: {
          sortKey: "createTime",
          asc: false,
          page: {
            pageNumber: 1,
            totalSize: 0,
            pageSize: 10
          },
          state: -1
        },
        delIdAry: [] //  定义批量删除id数组
      };
    },
    computed: {},
    methods: {
      getArticleCategory(myData) {
        if (myData) {
          this.articleCategory = JSON.parse(JSON.stringify(myData));
        } else {
          if (this.articleCategoryRoot) {
            let rootNode = this.articleCategoryRoot;
            if (rootNode && rootNode.children) {
              this.articleCategory = JSON.parse(JSON.stringify(rootNode.children));
            }
          } else {
            setTimeout(() => {
              this.getArticleCategory();
            }, 1000);
          }
        }
      },
      freshAllCategorys(children) {
        if (children) {
          children.forEach(item => {
            if (item.checked) {
              item.checked = !item.checked;
            } else {
              item.checked = true;
            }
            this.freshAllCategorys(item.children);
          });
        }
      },
      pickAllCategory() {
        this.freshAllCategorys(this.articleCategory);
        setTimeout(() => {
          this.getArticleCategory(this.articleCategory);
          this.getData();
        }, 0);
      },
      freshData() {
        this.filterData.page = {
          pageNumber: 1,
          totalSize: 0,
          pageSize: 10
        };
        this.getData();
      },
      setCategory() {
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
          data: this.editArticleData,
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
        if (this.$refs["catetree"]) {
          let filterCates = this.$refs["catetree"].getCheckedNodes();
          if (filterCates && filterCates.length > 0) {
            this.$data.filterData.categorys = filterCates.map(item => {
              return item.id;
            });
          } else {
            this.$data.filterData.categorys = null;
          }
        }
        this.$http.request({
          url: "/article/query",
          data: this.$data.filterData,
          method: "post"
        }).then(res => {
          this.articleData = res.list.map((val) => {
            val.authorName = val.author ? val.author.userName : "-";
            val.createDate = formatDate(val.createTime);
            return val;
          });
          this.filterData.page.totalSize = res.totalSize;
          this.$Notice.success({
            title: "已获取最新文章数据",
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
      // 获取标签
      this.$store.dispatch("init_module_article_tag");//.then(() => this.getArticleTag());
      // 获取分类
      this.$store.dispatch("init_module_article_category").then(() => this.getArticleCategory());
    },
    destroyed() {
    }
  };
</script>
<style lang="less">
</style>
