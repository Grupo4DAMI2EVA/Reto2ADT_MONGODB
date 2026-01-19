/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import config.MongoConfig;
import exception.OurException;
import java.util.ArrayList;
import model.*;
import org.bson.Document;

/**
 *
 * @author 2dami
 */
public class MongoImplements implements ModelDAO {
    private final MongoCollection<Document> col;

    public MongoImplements() {
        this.col = MongoConfig.getDatabase().getCollection("users");
    }

    @Override
    public User login(String username, String password) {
        Document userDoc = col.find(
            Filters.and(
                eq("username", username),
                eq("password", password)
            )
        ).first();

        if (userDoc != null) {
            User user = new User();
            user.setId(userDoc.getInteger("id"));
            user.setUsername(userDoc.getString("username"));
            user.setPassword(userDoc.getString("password"));
            user.setEmail(userDoc.getString("email"));
            // set other fields as needed
            return user;
        }
        return null;
    }

    @Override
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        for (Document doc : col.find()) {
            User user = new User();
            user.setId(doc.getInteger("id"));
            user.setUsername(doc.getString("username"));
            user.setPassword(doc.getString("password"));
            user.setEmail(doc.getString("email"));
            String genderStr = doc.getString("gender");
            Gender gender = null;
            if (genderStr != null) {
                switch (genderStr.toUpperCase()) {
                    case "MALE":
                        gender = Gender.MALE;
                        break;
                    case "FEMALE":
                        gender = Gender.FEMALE;
                        break;
                    case "OTHER":
                        gender = Gender.OTHER;
                        break;
                    default:
                        gender = null;
                        break;
                }
            }
            user.setGender(gender);
            user.setName(doc.getString("name"));
            user.setLastname(doc.getString("lastname"));
            user.setTelephone(doc.getString("telephone"));
            user.setCard(doc.getString("card"));
            users.add(user);
        }
        return users;
    }

    @Override
    public boolean updateUser(User user) throws OurException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteUser(int id) throws OurException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User register(User user) throws OurException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    }
