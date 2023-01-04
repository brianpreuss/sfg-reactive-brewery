package guru.springframework.sfgrestbrewery.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 2019-05-25.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Beer {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
  private UUID id;

  @Version
  private Long version;

  private String beerName;
  private BeerStyleEnum beerStyle;
  private String upc;

  private Integer quantityOnHand;
  private BigDecimal price;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdDate;

  @UpdateTimestamp
  private Timestamp lastModifiedDate;
}
