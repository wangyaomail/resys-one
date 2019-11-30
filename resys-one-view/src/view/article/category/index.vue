<!--文章分类管理-->
<template>
  <div>
    <Row>
      <i-col span="18">
        <Card :bordered="false" :dis-hover="true" class="card-tree">
          <p slot="title">
            <Icon type="md-filing"></Icon>
            文章分类
          </p>
          <Button slot="extra" type="primary" shape="circle" @click="freshData">
            <Icon type="md-refresh"></Icon>
            刷新
          </Button>
          <div class="classification-con">
            <div>
              <Button type="info" @click="modal1 = true, addObj.parent = 'root'">添加第一级分类
              </Button>
              <br/><br/>
            </div>
            <Tree :data="articleCategory" @on-select-change="selectTreeCatagory"
                  :render="renderContent"></Tree>
          </div>
        </Card>
      </i-col>
      <i-col span="6">
        <Card :bordered="false" :dis-hover="true" class="card-tree">
          <p style="text-align:center;font-size:1.3rem;">分类管理说明</p>
          <p>1、“分类父类”不可修改或删除。“分类名称”下填的名称就是我们平时的称呼，如首页新闻或热点专题，且不可修改，如果不正确可以随时删除重新添加；</p>
          <p>2、分类因为可以重复，因此分类名必须带路径。</p>
          <p>3、暂时不要建第四级标签。</p>
        </Card>
      </i-col>
      <Modal v-model="modal1" title="添加" @on-ok="addNewCategory">
        <Form :model="addObj" label-position="left" :label-width="100">
          <FormItem label="分类父类">
            <Input v-model="addObj.parent" placeholder="顶级分类请写root" readonly/>
          </FormItem>
          <FormItem label="分类名称">
            <Input v-model="addObj.id" placeholder="例如：导航-生活"/>
          </FormItem>
        </Form>
      </Modal>
      <Modal v-model="modal2" title="修改" @on-ok="updateCategory">
        <Form :model="updateObj" label-position="left" :label-width="100">
          <FormItem label="分类父类">
            <Input v-model="updateObj.parent" placeholder="顶级分类请写root" readonly/>
          </FormItem>
          <FormItem label="分类名称">
            <Input v-model="updateObj.id" placeholder="例如：导航-生活"/>
          </FormItem>
        </Form>
      </Modal>
    </Row>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        dataName: "articlecategory",
        newCategory: this.generateBlankTag(),
        modal1: false,
        modal2: false,
        addObj: {
          id: "",
          parent: ""
        },
        updateObj: {
          id: "",
          oldId:"",
          parent: ""
        }
      };
    },
    computed: {
      articleCategory() {
        let rootNode = this.$store.getters.articleCategoryRoot;
        if (rootNode && rootNode.children) {
          return JSON.parse(JSON.stringify(rootNode.children));
        } else {
          return [];
        }
      }
    },
    methods: {
      generateBlankTag() {
        return {
          id: "",
          title: "",
          parent: "",
          expand: true
        };
      },
      freshData() {
        this.$store.dispatch("init_module_article_category");
      },
      selectTreeCatagory(item) {
        if (item[0]) {
          this.$data.newCategory = item[0];
        }
      },
      addNewCategory() {
        let reg = /^[0-9]+.?[0-9]*$/;
        if (!reg.test(this.addObj.id)) {
          this.$http.request({
            url: "/articlecategory",
            data: this.addObj,
            method: "put"
          }).then(() => {
            this.$Notice.success({
              title: "添加分类成功"
            });
            this.freshData();
          });
        } else {
          this.$Notice.error({
            title: "分类名称不能是纯数字"
          });
        }
      },
      updateCategory() {
        this.$http.request({
          url: "/articlecategory/update",
          data: this.updateObj,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改分类成功"
          });
          this.freshData();
        });
      },
      delrecursive(delId) {
        this.$http.request({
          url: "/articlecategory/delrecursive",
          params: {id: delId},
          method: "get"
        }).then(() => {
          this.$Notice.success({
            title: "删除该分类和其所有子分类成功"
          });
          this.freshData();
        });
      },
      renderContent(h, {root, node, data}) {
        return h("span", {
          style: {
            display: "inline-block",
            width: "94%"
          }
        }, [
          h("span", {
            style: {fontSize: "14px", color: "#666"}
          }, "【" + data.title + "】"),
          h("span", {
            style: {
              display: "inline-block",
              float: "right",
              marginRight: "12px"
            }
          }, [
            h("Button", {
              props: Object.assign({}, this.buttonProps, {
                icon: "md-add",
                size: "small"
              }),
              style: {
                marginRight: "8px"
              },
              on: {
                click: () => {
                  this.modal1 = true;
                  this.addObj.id = "";
                  this.addObj.parent = data.title;
                }
              }
            }, "添加子类"),
            h("Button", {
              props: Object.assign({}, this.buttonProps, {
                icon: "md-options",
                size: "small"
              }),
              style: {
                marginRight: "8px"
              },
              on: {
                click: () => {
                  this.modal2 = true;
                  this.updateObj.id = data.title;
                  this.updateObj.oldId = data.title;
                  this.updateObj.parent = data.parent;
                }
              }
            }, "修改"),
            h("Button", {
              props: Object.assign({}, this.buttonProps, {
                icon: "md-close",
                size: "small"
              }),
              on: {
                click: () => {
                  this.$Modal.confirm({
                    title: "删除确认",
                    content: "<p>是否确认删除分类【<string>" + data.id + "</strong>】？</p><p>删除会递归删除该分类下的所有子分类，且操作不可恢复，请谨慎操作！</p>",
                    onOk: () => {
                      this.delrecursive(data.id);
                    }
                  });
                }
              }
            }, "删除")
          ])
        ]);
      }
    },
    created() {
    },
    mounted() {
      //  获取文章分类
      this.$store.dispatch("init_module_article_category");
    }
  };
</script>

<style lang="less" scoped>
  @import "../article.less";

  /deep/ span + span:hover {
    background: #f5f5f5;
  }

  .classification-con {
    margin-top: -20px;
    padding: 10px;
    overflow: auto;
  }

  .card-tree {
    margin: 5px;
  }

</style>
