package pa.soportehub.domain.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;
    private UUID organizationId;          // null solo para el Super Administrador
    private String email;
    private String passwordHash;          // null mientras está INVITED
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    private User(UUID organizationId, String email, String fullName, UserStatus status, String passwordHash, Role role) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.email = email.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.status = status;
        this.passwordHash = passwordHash;
        this.roles.add(role);
    }

    /** Usuario recién invitado: sin contraseña hasta que acepte la invitación. */
    public static User invited(UUID organizationId, String email, String fullName, Role role) {
        return new User(organizationId, email, fullName, UserStatus.INVITED, null, role);
    }

    /** Usuario ya activo (lo usan el sembrado de datos y las pruebas). */
    public static User active(UUID organizationId, String email, String fullName, String passwordHash, Role role) {
        return new User(organizationId, email, fullName, UserStatus.ACTIVE, passwordHash, role);
    }

    public void activate(String passwordHash) {
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public UserStatus getStatus() { return status; }
    public Set<Role> getRoles() { return roles; }
}
