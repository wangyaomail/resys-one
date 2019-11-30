<template>
  <div>
    <Row>
      <Card v-if="articleData">
        <Tag slot="title" type="dot" size="medium"
             :color="articleStateMapRevert[articleData.state].type">
          {{ articleStateMapRevert[articleData.state].name }}
        </Tag>
        <span slot="title">
        {{articleData.title}}
        </span>
        <Button slot="extra" type="primary" shape="circle" @click="$router.push({name:'home'})">
          <Icon type="md-arrow-back"></Icon>
          回推荐列表
        </Button>
        <Button slot="extra" type="default" shape="circle"
                @click="closeTag({name:'feed',params: {id: $route.params.id}})">
          <Icon type="md-close"></Icon>
          关闭当前标签
        </Button>
        <h1 style="text-align: center">{{articleData.title}}</h1>
        <h4 class="content-subtitle">
          {{ "作者:"+articleData.articleAuthor+"&nbsp;&nbsp;&nbsp;&nbsp;"
          +"来源:"+articleData.source+"&nbsp;&nbsp;&nbsp;&nbsp;"
          +"时间:"+formatDate(articleData.publishTime)}}
        </h4>
        <div class="main-content" v-html="articleData.content"></div>
      </Card>
    </Row>
    <Row style="margin-top: 15px">
      <Card>
        <p slot="title">
          <Icon type="md-paper"></Icon>
          相关推荐
        </p>
        <List item-layout="vertical">
          <ListItem v-for="item in correlations" :key="item.id">
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
    </Row>
  </div>
</template>

<script>
  import {mapMutations, mapGetters} from "vuex";
  import {formatDate} from "@/libs/filters";

  export default {
    name: "params",
    data() {
      return {
        articleData: null,
        correlations: []
      };
    },
    computed: {
      ...mapGetters([
        "articleStateMapRevert"
      ])
    },
    methods: {
      ...mapMutations([
        "closeTag"
      ]),
      formatDate(time) {
        return formatDate(time);
      },
      getData() {
        // 获取正文
        let postData = JSON.parse(sessionStorage.getItem("click-" + this.$route.params.id));
        this.$http.request({
          url: "/resys/getArticle",
          data: postData,
          method: "post"
        }).then(res => {
          console.log(res);
          this.articleData = res;
        });
        // 获取相关推荐
        this.$http.request({
          url: "/resys/getArticleCorrelation",
          params: {
            "articleId": this.$route.params.id,
            "limit": 15
          },
          method: "get"
        }).then(res => {
          console.log(res);
          this.correlations = res;
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
        // 如果是点击的相关新闻，都算一个recid下的点击，但不需要保存列表记录
        sessionStorage.setItem("click-" + itemid, JSON.stringify({
          "click": itemid,
          "recid": sessionStorage.recid
        }));
        this.$router.push(route);
      }
    },
    watch: {
      $route: function (newVal, oldVal) {
        console.log("123");
        if (oldVal === null || oldVal !== this.$route.params.id) {
          this.getData();
        }
      }
    },
    created() {
      this.getData("created");
    }
  };
</script>

<style>
  .content-subtitle {
    color: #75664D;
    text-align: center;
    margin-top: 5px;
    margin-bottom: 15px;
  }

  .main-content p {
    font-size: 16px;
    line-height: 28px;
    padding-left: 10px;
    padding-right: 10px;
    margin-bottom: 20px;
    color: #222;
  }

  .main-content img {
    text-align: center;
    margin: 0 auto;
    width: auto;
    min-height: 200px;
    max-height: 500px;
    display: block;
    padding-left: 10px;
    padding-right: 10px;
  }
</style>
