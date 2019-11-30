<template>
  <div>
    <Row>
      <i-col span="18" style="padding-right: 12px;">
        <Form :model="addData" label-position="right" :label-width="80">
          <FormItem label="文章标题">
            <i-input v-model="addData.title"></i-input>
          </FormItem>
          <FormItem label="原文作者">
            <i-input v-model="addData.articleAuthor"></i-input>
          </FormItem>
          <FormItem label="文章来源">
            <i-input v-model="addData.source"></i-input>
          </FormItem>
          <FormItem label="文章状态">
            <RadioGroup v-model="addData.state" type="button">
              <Radio v-for="item in articleStateList" :value="item.value"
                     :key="item.value" :label="item.value">{{item.name}}
              </Radio>
            </RadioGroup>
          </FormItem>
          <FormItem label="分类">
            <Tag
              v-for="item in addData.categorys"
              :key="item.id"
              type="border"
              :color="item.color"
            >{{item.id}}
            </Tag>
          </FormItem>
          <FormItem label="标签">
            <Tag
              v-for="item in addData.tags"
              :key="item.id"
              type="border"
              :color="item.color"
            >{{item.id}}
            </Tag>
          </FormItem>
        </Form>
        <Divider>文章正文</Divider>
        <editor ref="editor" :value="articleContent" :cache="false"
                @on-change="handleEditorChange"/>
      </i-col>
      <i-col span="6" class="padding-left-10">
        <Row style="text-align: center;">
            <span class="publish-button">
              <Button
                long
                @click="handlePublish"
                :loading="publishLoading"
                icon="md-checkmark"
                type="success"
              >修改</Button>
            </span>
        </Row>
        <Divider/>
        <Tabs size="small" type="card">
          <TabPane label="分类">
            <div class="classification-con">
              <Tree
                :data="articleCategory"
                @on-check-change="setCategory"
                show-checkbox
                check-directly
                ref="catetree"
              ></Tree>
            </div>
          </TabPane>
          <TabPane label="标签">
            <div class="classification-con">
              <Tree
                :data="articleTag"
                @on-check-change="setTag"
                show-checkbox
                check-directly
                ref="catetree"
              ></Tree>
            </div>
          </TabPane>
        </Tabs>
      </i-col>
    </Row>
  </div>
</template>

<script>
  import {mapGetters} from "vuex";
  import Editor from "_c/editor";

  export default {
    name: "article-update-editor",
    components: {Editor},
    props: ["srcid", "closemodal"],
    data() {
      return {
        addData: {
          title: "",
          content: "",
          state: 2,
          source: "",
          articleAuthor: "",
          categorys: [],
          tags: []
        },
        publishLoading: false,
        articleCategory: [],
        articleTag: [],
        articleContent: ""
      };
    },
    computed: {
      ...mapGetters([
        "articleCategoryRoot",
        "articleStateList",
        "tagRoot"
      ])
    },
    methods: {
      generateBlankArticle() {
        sessionStorage.editorCache = "";
        return {
          title: "",
          content: "",
          state: 2,
          source: "",
          articleAuthor: this.$store.state.user.user.id,
          categorys: [],
          tags: []
        };
      },
      getArticleCategory() {
        let theMap = {};
        if (this.addData.categorys) {
          this.addData.categorys.forEach(item => {
            theMap[item.id] = item;
          });
        }
        let rootNode = this.$store.getters.articleCategoryRoot;
        if (rootNode && rootNode.children) {
          let rootNodeChild = JSON.parse(JSON.stringify(rootNode.children));
          this.restoreAllTree(rootNodeChild, theMap);
          this.articleCategory = rootNodeChild;
        } else {
          this.articleCategory = [];
        }
      },
      getArticleTag() {
        let theMap = {};
        if (this.addData.tags) {
          this.addData.tags.forEach(item => {
            theMap[item.id] = item;
          });
        }
        let rootNode = this.$store.getters.tagRoot;
        if (rootNode && rootNode.children) {
          let rootNodeChild = JSON.parse(JSON.stringify(rootNode.children));
          this.restoreAllTree(rootNodeChild, theMap);
          this.articleTag = rootNodeChild;
        } else {
          this.articleTag = [];
        }
      },
      restoreAllTree(children, theMap) {
        if (children) {
          children.forEach(item => {
            if (theMap[item.id] && theMap[item.id].color === "red") {
              item.checked = true;
            }
            this.restoreAllTree(item.children, theMap);
          });
        }
      },
      findTreeParent(nodes, item, dataMap) {
        if (item && item.parent && item.parent !== "root") {
          nodes[item.parent] = {
            id: item.parent,
            color: "green"
          };
          this.findTreeParent(nodes, item.parent, dataMap);
        }
      },
      setTreePicked(selected, dataMap) {
        let nodes = {}, arr = [];
        selected.map(item => {
          this.findTreeParent(nodes, item, dataMap);
          nodes[item.id] = {
            id: item.id,
            color: "red"
          };
        });
        for (let i in nodes) {
          arr.push(nodes[i]);
        }
        return arr;
      },
      setCategory(selected) {
        this.addData.categorys = this.setTreePicked(selected, this.$store.getters.articleCategoryMap);
      },
      setTag(selected) {
        this.addData.tags = this.setTreePicked(selected, this.$store.getters.tagMap);
      },
      handleEditorChange(html, text) {
        this.addData.content = html;
      },
      canPublish() {
        if (this.addData.title.length === 0) {
          this.$Message.error("请输入文章标题");
          return false;
        } else if (!this.addData.categorys || this.addData.categorys.length === 0) {
          this.$Message.error("文章分类不能为空");
          return false;
        } else {
          if (this.addData.content && this.addData.content.length > 30) {
            return true;
          } else {
            this.$Message.error("文章内容长度不够30");
            return false;
          }
        }
      },
      handlePublish() {
        if (this.canPublish()) {
          this.publishLoading = true;
          let postMsg = JSON.parse(JSON.stringify(this.addData));
          postMsg.content = postMsg.content.replace(
            /\<br \/>/g,
            "<br />&emsp;&emsp;"
          );
          postMsg.content = postMsg.content.replace(
            /\<p\>/g,
            "<p>&emsp;&nbsp;&nbsp;&nbsp;"
          );
          if (postMsg.state) {

          }
          if (postMsg.categorys) {
            postMsg.categorys = postMsg.categorys.map(item => {
              return item.id;
            });
          }
          if (postMsg.tags) {
            postMsg.tags = postMsg.tags.map(item => {
              return item.id;
            });
          }
          this.$http.request({url: "/article", data: postMsg, method: "put"}).then(res => {
            this.publishLoading = false;
            this.$Notice.success({
              title: "保存成功",
              desc: "文章《" + this.addData.title + "》保存成功"
            });
            this.addData = this.generateBlankArticle();
            this.$router.push("/article/list");
          }).catch(err => {
            this.publishLoading = false;
            this.$Notice.error({
              title: "保存失败",
              desc: err.msg
            });
          });
        }
      },
      getData() {
        this.$http.request({
          url: "/article/get",
          params: {id: this.srcid},
          method: "get"
        }).then(res => {
          this.addData = JSON.parse(JSON.stringify(res));
          let cmap = this.$store.getters.articleCategoryMap;
          if (this.addData.categorys) {
            this.addData.categorys = this.addData.categorys.map(item => {
              if (cmap[item]) {
                if (cmap[item].children) {
                  return {
                    id: item,
                    color: "green"
                  };
                } else {
                  return {
                    id: item,
                    color: "red"
                  };
                }
              }
            });
          }
          let tmap = this.$store.getters.tagMap;
          if (this.addData.tags) {
            this.addData.tags = this.addData.tags.map(item => {
              if (tmap[item]) {
                if (tmap[item].children) {
                  return {
                    id: item,
                    color: "green"
                  };
                } else {
                  return {
                    id: item,
                    color: "red"
                  };
                }
              }
            });
          }
          this.$refs.editor.setHtml(this.addData.content);
          this.getArticleCategory();
          this.getArticleTag();
          this.$Notice.success({
            title: "获取文章正文"
          });
        });
      }
    },
    watch: {
      closemodal(newVal, oldVal) {
        if (newVal && (!oldVal)) {
          this.getData();
        }
      }
    },
    mounted() {
    }
  };
</script>

<style lang="less" scoped>
  @import "../article.less";

  .preview {
    min-height: 200px;
    width: 100%;
  }

  .article-link-con {
    height: 32px;
    width: 100%;
  }

  .fixed-link-enter {
    opacity: 0;
  }

  .fixed-link-enter-active,
  .fixed-link-leave-active {
    transition: opacity 0.3s;
  }

  .fixed-link-enter-to {
    opacity: 1;
  }

  .openness-radio-con {
    margin-left: 40px;
    padding-left: 10px;
    height: 130px;
    border-left: 1px dashed #ebe9f3;
    overflow: hidden;
  }

  .publish-time-picker-con {
    margin-left: 40px;
    padding-left: 10px;
    height: 100px;
    border-left: 1px dashed #ebe9f3;
    overflow: hidden;
  }

  .openness-con-enter {
    height: 0;
  }

  .openness-con-enter-active,
  .openness-con-leave-active {
    transition: height 0.3s;
  }

  .openness-con-enter-to {
    height: 130px;
  }

  .openness-con-leave {
    height: 130px;
  }

  .openness-con-leave-to {
    height: 0;
  }

  .publish-button-con {
    border-top: 1px solid #f3eff1;
    padding-top: 14px;
    text-align: center;
  }

  .publish-button {
    margin-left: 10px;
  }

  .publish-time-enter {
    height: 0;
  }

  .publish-time-enter-active,
  .publish-time-leave-active {
    transition: height 0.3s;
  }

  .publish-time-enter-to {
    height: 100px;
  }

  .publish-time-leave {
    height: 100px;
  }

  .publish-time-leave-to {
    height: 0;
  }

  .classification-con {
    margin-top: -16px;
    padding: 10px;
    overflow: auto;
  }

  .add-new-tag-con {
    margin-top: 20px;
    border-top: 1px dashed #dbdddf;
    padding: 20px 0;
    height: 60px;
    overflow: hidden;
  }

  .add-new-tag-enter {
    height: 0;
    margin-top: 0;
    padding: 0px 0;
  }

  .add-new-tag-enter-active,
  .add-new-tag-leave-active {
    transition: all 0.3s;
  }

  .add-new-tag-enter-to {
    height: 60px;
    margin-top: 20px;
    padding: 20px 0;
  }

  .add-new-tag-leave {
    height: 60px;
    margin-top: 20px;
    padding: 20px 0;
  }

  .add-new-tag-leave-to {
    height: 0;
    margin-top: 0;
    padding: 0px 0;
  }
</style>

