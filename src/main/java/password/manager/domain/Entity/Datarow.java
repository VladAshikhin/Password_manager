package password.manager.domain.Entity;

import javafx.beans.property.SimpleStringProperty;
import lombok.Data;

import javax.persistence.*;

/**
 * Each user sees only his own datarows
 */
@Data
public class Datarow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private SimpleStringProperty url;

    @Column(nullable = false)
    private SimpleStringProperty login;

    @Column(nullable = false)
    private SimpleStringProperty password;

    private SimpleStringProperty notes;

    private Integer userId;

    public Datarow(String url, String login, String password, String notes) {
        this.url = new SimpleStringProperty(url);
        this.login = new SimpleStringProperty(login);
        this.password = new SimpleStringProperty(password);
        this.notes = new SimpleStringProperty(notes);
    }

    public Datarow(Integer id, String url, String login, String password,
                   String notes, Integer userId) {
        this.id = id;
        this.url = new SimpleStringProperty(url);
        this.login = new SimpleStringProperty(login);
        this.password = new SimpleStringProperty(password);
        this.notes = new SimpleStringProperty(notes);
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url.get();
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getLogin() {
        return login.get();
    }

    public SimpleStringProperty loginProperty() {
        return login;
    }

    public void setLogin(String login) {
        this.login.set(login);
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getNotes() {
        return notes.get();
    }

    public SimpleStringProperty notesProperty() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}