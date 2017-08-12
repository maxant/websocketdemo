package ch.maxant.websocketdemo.mcs.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "T_ROLE")
public class Role {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "ROLE_NAME", length = 100)
    private String roleName;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRoleName() {
        return roleName;
    }
}
