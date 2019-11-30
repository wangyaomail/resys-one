<!--文章列表-->
<template>
  <div>
    <Row>
      <i-col span="18" style="padding-right: 12px;">
        <Card>
          <p slot="title">
            <Icon type="md-paper"></Icon>
            我的推荐
          </p>
          <List item-layout="vertical">
            <ListItem v-for="item in reclist" :key="item.id">
              <a @click="handleTitleClick(item.id, item.title)">
                <ListItemMeta :itemid="item.id" :title="item.title"/>
              </a>
              <span style="color:#75664D;">{{ item.source +" "+item.publishTime}}</span>
              <template slot="extra">
                <Tag color="purple" v-for="tag in item.categorys" :key="tag">{{ tag }}</Tag>
              </template>
            </ListItem>
          </List>
        </Card>
      </i-col>
      <i-col span="6" class="padding-left-10">
        <Card>
          <p slot="title">
            <Icon type="md-paper-plane"></Icon>
            测试
          </p>
          <Row>
            <Row style="text-align: center;">
              <Button
                long
                @click="getFresh"
                :loading="getFreshLoading"
                icon="md-refresh"
                type="success"
              >个性化推荐结果
              </Button>
            </Row>
            <Divider/>
            <Row style="text-align: center;">
              <Button
                long
                @click="getHotNews"
                :loading="getHotLoading"
                icon="md-bonfire"
                type="success"
              >热度推荐
              </Button>
            </Row>
            <Divider/>
            <Row style="text-align: center;">
              <Button
                long
                @click="randomGenerateFeed"
                :loading="randomGenerateFeedLoading"
                icon="md-musical-note"
                type="info"
              >随机生成模拟数据
              </Button>
            </Row>
            <Divider/>
            <Row style="text-align: center;">
              <Button
                long
                @click="clearUserHistory"
                :loading="clearUserHistoryLoading"
                icon="md-trash"
                type="warning"
              >清空用户的浏览记录
              </Button>
            </Row>
            <Divider/>
            <Row style="text-align: center;">
              <Button
                long
                @click="exportAllArticle"
                icon="md-download"
                type="info"
              >导出 news-all 全量文件
              </Button>
            </Row>
            <Divider/>
            <Row style="text-align: center;">
              <Button
                long
                @click="exportAllTags"
                icon="md-download"
                type="info"
              >导出 tag-all 全量数据到MQ
              </Button>
            </Row>
          </Row>
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
        recid: "",
        reclist: [],
        getFreshLoading: false,
        getHotLoading: false,
        randomGenerateFeedLoading: false,
        clearUserHistoryLoading: false
      };
    },
    computed: {},
    methods: {
      getData() {
        this.getFresh();
      },
      getFresh() {
        this.getFreshLoading = true;
        this.$http.request({
          url: "/resys/getRecList",
          params: {
            "feedNumber": 15
          },
          method: "get"
        }).then(res => {
          console.log(res);
          this.recid = res.recid;
          this.reclist = res.list.map(item => {
            item.publishTime = formatDate(item.publishTime);
            return item;
          });
          this.getFreshLoading = false;
          this.$Notice.success({
            title: "已获取最新文章数据",
            duration: 1
          });
        });
      },
      getHotNews() {
        this.getHotLoading = true;
        this.$http.request({
          url: "/resys/getArticleHot",
          params: {
            "limit": 100
          },
          method: "get"
        }).then(res => {
          console.log(res);
          this.recid = res.recid;
          this.reclist = res.list.map(item => {
            item.publishTime = formatDate(item.publishTime);
            return item;
          });
          this.getHotLoading = false;
          this.$Notice.success({
            title: "已获取最新文章数据",
            duration: 1
          });
        });
      },
      handleTitleClick(itemid, itemtitle) {
        const route = {
          name: "feed",
          params: {
            id: itemid
            // title: itemtitle
          },
          meta: {
            title: itemid
          }
        };
        sessionStorage.setItem("click-" + itemid, JSON.stringify({
          "click": itemid,
          "recid": this.recid,
          "list": this.reclist.map(r => r.id)
        }));
        this.$router.push(route);
      },
      randomGenerateFeed() {
        this.randomGenerateFeedLoading = true;
        this.$http.request({
          url: "/resys/randomGenerateFeed",
          params: {
            "feedNumber": 30
          },
          method: "get"
        }).then(res => {
          console.log(res);
          this.randomGenerateFeedLoading = false;
          this.$Notice.success({
            title: res,
            duration: 1
          });
        });
      },
      clearUserHistory() {
        this.clearUserHistoryLoading = true;
        this.$http.request({
          url: "/resys/clearUserHistory",
          method: "get"
        }).then(res => {
          console.log(res);
          this.clearUserHistoryLoading = false;
          this.$Notice.success({
            title: res,
            duration: 1
          });
        });
      },
      exportAllArticle() {
        this.$http.request({
          url: "/article/exportAllArticle",
          method: "get"
        }).then(res => {
          console.log(res);
          this.$Notice.success({
            title: res,
            duration: 1
          });
        });
      },exportAllTags() {
        this.$http.request({
          url: "/tag/sendAllTags",
          method: "get"
        }).then(res => {
          console.log(res);
          this.$Notice.success({
            title: res,
            duration: 1
          });
        });
      }
    },
    mounted() {
      if (sessionStorage.reclist) {
        this.recid = sessionStorage.recid;
        this.reclist = JSON.parse(sessionStorage.reclist);
      } else {
        this.getData();
      }
    },
    destroyed() {
      sessionStorage.recid = this.recid;
      sessionStorage.reclist = JSON.stringify(this.reclist);
    }
  };
</script>
<style lang="less">
</style>
