package password.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Setter
@Getter
@Entity
@AllArgsConstructor
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String url;

    private String login;

    private String password;

    private String hiddenPassword;

    private String notes;

    private Integer userId;

    public Data(String url, String login, String password, String notes, Integer userId) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.notes = notes;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(url, data.url) && Objects.equals(login, data.login) && Objects.equals(password, data.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, login, password);
    }
}