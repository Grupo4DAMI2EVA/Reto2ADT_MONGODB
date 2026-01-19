/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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

    // SOLO DEBES TENER ESTE MÉTODO LOGIN:
    @Override
    public Profile login(String credential, String password) throws OurException {
        try {
            Document userDoc = col.find(
                    Filters.and(
                            Filters.or(
                                    eq("username", credential),
                                    eq("email", credential)
                            ),
                            eq("password", password)
                    )
            ).first();

            if (userDoc != null) {
                String role = userDoc.getString("role");

                if ("admin".equalsIgnoreCase(role)) {
                    // Crear objeto Admin
                    Admin admin = new Admin();

                    // Manejar el _id
                    Object idObj = userDoc.get("_id");
                    if (idObj instanceof Integer) {
                        admin.setId((Integer) idObj);
                    } else if (idObj instanceof Double) {
                        admin.setId(((Double) idObj).intValue());
                    } else {
                        admin.setId(-1);
                    }

                    admin.setUsername(userDoc.getString("username"));
                    admin.setPassword(userDoc.getString("password"));
                    admin.setEmail(userDoc.getString("email"));
                    admin.setName(userDoc.getString("name"));
                    admin.setLastname(userDoc.getString("lastname"));
                    admin.setTelephone(userDoc.getString("telephone"));

                    // Obtener admin_data si existe
                    Document adminData = (Document) userDoc.get("admin_data");
                    if (adminData != null) {
                        String currentAccount = adminData.getString("current_account");
                        if (currentAccount != null) {
                            admin.setCurrent_account(currentAccount);
                        }
                    }

                    return admin;
                } else {
                    // Crear objeto User (código existente)
                    User user = new User();
                    Object idObj = userDoc.get("_id");
                    if (idObj instanceof Integer) {
                        user.setId((Integer) idObj);
                    } else if (idObj instanceof Double) {
                        user.setId(((Double) idObj).intValue());
                    } else {
                        user.setId(-1);
                    }

                    user.setUsername(userDoc.getString("username"));
                    user.setPassword(userDoc.getString("password"));
                    user.setEmail(userDoc.getString("email"));
                    user.setName(userDoc.getString("name"));
                    user.setLastname(userDoc.getString("lastname"));
                    user.setTelephone(userDoc.getString("telephone"));

                    Document userData = (Document) userDoc.get("user_data");
                    if (userData != null) {
                        String genderStr = userData.getString("gender");
                        if (genderStr != null && !genderStr.isEmpty()) {
                            try {
                                user.setGender(Gender.valueOf(genderStr.toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                user.setGender(null);
                            }
                        }

                        String card = userData.getString("card");
                        if (card != null) {
                            user.setCard(card);
                        }
                    }

                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OurException("Error during login: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<User> getUsers() throws OurException {
        try {
            ArrayList<User> users = new ArrayList<>();
            for (Document doc : col.find()) {
                // Solo procesar documentos con role "user" o sin role
                String role = doc.getString("role");
                if (role != null && "admin".equalsIgnoreCase(role)) {
                    continue; // Saltar admins
                }

                User user = new User();

                Object idObj = doc.get("_id");
                if (idObj instanceof Integer) {
                    user.setId((Integer) idObj);
                } else if (idObj instanceof Double) {
                    user.setId(((Double) idObj).intValue());
                } else {
                    user.setId(-1);
                }

                user.setUsername(doc.getString("username"));
                user.setPassword(doc.getString("password"));
                user.setEmail(doc.getString("email"));
                user.setName(doc.getString("name"));
                user.setLastname(doc.getString("lastname"));
                user.setTelephone(doc.getString("telephone"));

                Document userData = (Document) doc.get("user_data");
                if (userData != null) {
                    String genderStr = userData.getString("gender");
                    if (genderStr != null) {
                        switch (genderStr.toUpperCase()) {
                            case "MALE":
                                user.setGender(Gender.MALE);
                                break;
                            case "FEMALE":
                                user.setGender(Gender.FEMALE);
                                break;
                            case "OTHER":
                                user.setGender(Gender.OTHER);
                                break;
                            default:
                                user.setGender(null);
                                break;
                        }
                    }
                    user.setCard(userData.getString("card"));
                }

                users.add(user);
            }
            return users;
        } catch (Exception e) {
            throw new OurException("Error getting users: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUser(User user) throws OurException {
        try {
            // Crear el objeto user_data
            Document userData = new Document();
            userData.append("gender", user.getGender() != null ? user.getGender().name() : null);
            userData.append("card", user.getCard());

            // Crear un documento con todos los campos actualizados
            Document updateDoc = new Document();
            updateDoc.append("username", user.getUsername());
            updateDoc.append("password", user.getPassword());
            updateDoc.append("email", user.getEmail());
            updateDoc.append("name", user.getName());
            updateDoc.append("lastname", user.getLastname());
            updateDoc.append("telephone", user.getTelephone());
            updateDoc.append("user_data", userData);

            // Crear el documento de actualización con $set
            Document updateOperation = new Document("$set", updateDoc);

            // Ejecutar la actualización usando "_id"
            UpdateResult result = col.updateOne(
                    Filters.eq("_id", user.getId()),
                    updateOperation
            );

            return result.getModifiedCount() > 0;

        } catch (Exception e) {
            throw new OurException("Error updating user in MongoDB: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteUser(int id) throws OurException {
        try {
            DeleteResult result = col.deleteOne(Filters.eq("_id", id));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            throw new OurException("Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public User register(User user) throws OurException {
        try {
            // Verificar si el usuario ya existe
            Document existingUser = col.find(
                    Filters.or(
                            eq("username", user.getUsername()),
                            eq("email", user.getEmail())
                    )
            ).first();

            if (existingUser != null) {
                throw new OurException("Username or email already exists");
            }

            // Obtener el próximo ID buscando el máximo _id actual
            int nextId = 1;

            // Buscar el documento con el _id más alto
            Document maxIdDoc = col.find()
                    .sort(new Document("_id", -1)) // Ordenar descendente por _id
                    .limit(1)
                    .first();

            if (maxIdDoc != null) {
                Object maxIdObj = maxIdDoc.get("_id");
                if (maxIdObj instanceof Integer) {
                    nextId = (Integer) maxIdObj + 1;
                } else if (maxIdObj instanceof Double) {
                    nextId = ((Double) maxIdObj).intValue() + 1;
                }
            }

            user.setId(nextId);

            // Crear objeto user_data
            Document userData = new Document();
            userData.append("gender", user.getGender() != null ? user.getGender().name() : null);
            userData.append("card", user.getCard());

            // Crear documento MongoDB con la estructura correcta
            Document userDoc = new Document();
            userDoc.append("_id", user.getId());
            userDoc.append("username", user.getUsername());
            userDoc.append("password", user.getPassword());
            userDoc.append("email", user.getEmail());
            userDoc.append("name", user.getName());
            userDoc.append("lastname", user.getLastname());
            userDoc.append("telephone", user.getTelephone());
            userDoc.append("role", "user"); // Asigna rol por defecto
            userDoc.append("user_data", userData);

            // Insertar en la colección
            col.insertOne(userDoc);

            return user;

        } catch (Exception e) {
            throw new OurException("Error registering user: " + e.getMessage());
        }
    }
}
