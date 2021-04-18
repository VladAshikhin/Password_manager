package password.manager.entity;

import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "DATA")
public class Data {

    //NB: Each user sees only his own datarows
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "notes")
    private String notes;

    @Column(name = "userId")
    private Integer userId;

    public Data(String url, String login, String password, String notes) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.notes = notes;
    }

    public Data(Integer id, String url, String login, String password, String notes, Integer userId) {
        this.id = id;
        this.url = url;
        this.login = login;
        this.password = password;
        this.notes = notes;
        this.userId = userId;
    }
}