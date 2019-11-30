<!--文章标签管理-->
<template>
  <Row>
    <i-col span="18">
      <Card :bordered="false" :dis-hover="true" class="card-tree">
        <p slot="title">
          <Icon type="md-pricetag"></Icon>
          标签树
        </p>
        <Button slot="extra" type="primary" shape="circle" @click="freshData">
          <Icon type="md-refresh"></Icon>
          刷新
        </Button>
        <div class="classification-con">
          <div>
            <Button type="info" @click="modal1 = true, addObj.parent = 'root'">添加第一级标签
            </Button>
            <br/><br/>
          </div>
          <Tree :data="tagTree" :load-data="loadTagSubTree" @on-select-change="selectTreeTag"
                :render="renderContent"></Tree>
        </div>
      </Card>
    </i-col>
    <i-col span="6">
      <Card :bordered="false" :dis-hover="true" class="card-tree">
        <p style="text-align:center;font-size:1.3rem;">标签管理说明</p>
        <p>1、最低级标签不支持修改，如不正确可删除重新添加；</p>
        <p>2、标签名全局唯一，因此标签名不需要带路径，但同一标签不能在不同的标签下。</p>
        <p>3、暂时不要建第四级标签。</p>
      </Card>
    </i-col>
    <Modal v-model="modal1" title="添加" @on-ok="addNewTag">
      <Form :model="addObj" label-position="left" :label-width="100">
        <FormItem label="标签父类">
          <Input v-model="addObj.parent" placeholder="顶级标签请写root" readonly/>
        </FormItem>
        <FormItem label="标签名称">
          <Input v-model="addObj.id" placeholder="例如：题材"/>
        </FormItem>
        <FormItem label="权重">
          <Input v-model="addObj.w" placeholder="例如：0.5"/>
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="modal2" title="修改" @on-ok="updateTag">
      <Form :model="updateObj" label-position="left" :label-width="100">
        <FormItem label="标签父类">
          <Input v-model="updateObj.parent" placeholder="顶级标签请写root" readonly/>
        </FormItem>
        <FormItem label="标签名称">
          <Input v-model="updateObj.id" placeholder="例如：题材"/>
        </FormItem>
        <FormItem label="权重">
          <Input v-model="updateObj.w" placeholder="例如：0.5"/>
        </FormItem>
      </Form>
    </Modal>
  </Row>
</template>

<script>
  import {mapGetters} from "vuex";

  export default {
    data() {
      return {
        newTag: this.generateBlankTag(),
        modal1: false,
        modal2: false,
        addObj: {
          id: "",
          parent: ""
        },
        updateObj: {
          id: "",
          oldid: "",
          parent: ""
        }
      };
    },
    computed: {
      tagTree() {
        let rootNode = this.$store.getters.tagRoot;
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
        this.$store.dispatch("init_module_article_tag");
      },
      selectTreeTag(item) {
        if (item[0]) {
          this.$data.newTag = item[0];
        }
      },
      loadTagSubTree(item, callback) {
        this.$http.request({
          url: "/tag/parent",
          params: {parent: item.title},
          method: "get"
        }).then((res) => {
          if (res) {
            callback(res.map((val) => {
              return {
                title: val.id,
                loading: false,
                expand: true,
                children: [],
                parent: val.parent
              };
            }));
          } else {
            callback([]);
          }
        });
      },
      addNewTag() {
        let reg = /^[0-9]+.?[0-9]*$/;
        if (!reg.test(this.addObj.id)) {
          this.$http.request({
            url: "/tag",
            data: this.addObj,
            method: "put"
          }).then(() => {
            this.$Notice.success({
              title: "添加标签成功"
            });
            this.freshData();
          });
        } else {
          this.$Notice.error({
            title: "标签名称不能是纯数字"
          });
        }
      },
      updateTag() {
        this.$http.request({
          url: "/tag/upsert",
          data: this.updateObj,
          method: "post"
        }).then(() => {
          this.$Notice.success({
            title: "修改标签成功"
          });
          this.freshData();
        });
      },
      delrecursive(delId) {
        this.$http.request({
          url: "/tag/delrecursive",
          params: {id: delId},
          method: "get"
        }).then(() => {
          this.$Notice.success({
            title: "删除该标签和其所有子标签成功"
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
          }, "【" + data.title + "】：" + data.w.toFixed(4)),
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
                  this.addObj.id = "";
                  this.addObj.parent = data.title;
                  this.modal1 = true;
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
                  this.updateObj = JSON.parse(JSON.stringify(data));
                  this.updateObj.id = data.title;
                  this.updateObj.oldId = data.title;
                  this.modal2 = true;
                }
              }
            }, "修改"),
            h("Button", {
              props: Object.assign({}, this.buttonProps, {
                icon: "ios-minus-empty",
                size: "small"
              }),
              on: {
                click: () => {
                  this.$Modal.confirm({
                    title: "删除确认",
                    content: "<p>是否确认删除标签【<string>" + data.id + "</strong>】？</p><p>删除会递归删除该标签下的所有子标签，且操作不可恢复，请谨慎操作！</p>",
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
      // 获取标签
      this.$store.dispatch("init_module_article_tag");
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
