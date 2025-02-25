package pl.gov.coi.pomocua.ads;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.gov.coi.pomocua.ads.phone.PhoneUtil;
import pl.gov.coi.pomocua.ads.users.User;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PostLoad;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Audited
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonBaseOfferInheritance
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseOffer <T extends BaseOfferVM> {
    public static final String TITLE_ALLOWED_TEXT = "^[^'\"%<>()@]*$";
    public static final String DESCRIPTION_ALLOWED_TEXT = "^[^'\"%<>]*$";

    @Id
    @NotNull
    @GeneratedValue(strategy = SEQUENCE)
    public Long id;

    @Embedded
    @JsonIgnore
    public UserId userId;

    @NotNull
    public String userFirstName;

    @NotBlank
    @Length(max = 80)
    @Pattern(regexp = TITLE_ALLOWED_TEXT)
    public String title;

    @NotBlank
    @Length(max = 2000)
    @Pattern(regexp = DESCRIPTION_ALLOWED_TEXT)
    public String description;

    public String phoneNumber;

    public String phoneCountryCode;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    public Instant modifiedDate;

    @JsonIgnore
    @NotNull
    @Enumerated(EnumType.STRING)
    public Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    public Language detectedLanguage;

    public String titleUa;
    public String descriptionUa;

    public String titleEn;
    public String descriptionEn;

    public String titleRu;
    public String descriptionRu;

    public Byte translationErrorCounter = 0;

    public enum Status {
        ACTIVE, INACTIVE
    }

    public void attachTo(User user) {
        this.userId = user.id();
        this.userFirstName = user.firstName();
    }

    @PostLoad
    private void onLoad() {
        if (StringUtils.isNotEmpty(phoneCountryCode)) return;

        PhoneUtil.getPhoneDetails(phoneNumber).ifPresent(phoneDetails -> {
            phoneCountryCode = phoneDetails.countryCode();
            phoneNumber = phoneDetails.nationalNumber();
        });
    }

    @JsonIgnore
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public abstract T accept(BaseOfferVisitor visitor, Language viewLang);
}
