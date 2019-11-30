package xmt.resys.user.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.user.bean.mongo.HBUser;

@Repository("userDao")
public class UserDao extends BaseMongoDao<HBUser> {

    public HBUser findUserByNameAndPwd(String username,
                                       String password) {
        Query query = new Query();
        Criteria c1 = Criteria.where("name").is(username);
        Criteria c2 = Criteria.where("password").is(password);
        query.addCriteria(new Criteria().andOperator(c1, c2));
        return mongoTemplate.findOne(query, getClassT(), getCollectionName());
    }

    public HBUser findUserById(String id) {
        return findOne(id);
    }

    public HBUser findUserByName(String name) {
        return mongoTemplate.findOne(new Query(Criteria.where("userName").is(name)),
                                     getClassT(),
                                     getCollectionName());
    }

    public HBUser insert(HBUser object) {
        return super.insert(object);
    }
}
