package com.example.demo;

import com.example.demo.models.Pand;
import com.example.demo.repository.PandsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PandRepositoryTest {

    @Autowired
    PandsRepository pandsRepository;

    @Test
    public void pandRepositorySave(){

        //Arrange
        Pand pand = Pand.builder()
                .pandCode("PN001").mainQuantity(1000)
                .height("2").width("2").build();

        //Act
        Pand savedPand = pandsRepository.save(pand);

        //Assert
        Assertions.assertThat(savedPand).isNotNull();
        Assertions.assertThat(savedPand.getId()).isGreaterThan(0);
    }

}
