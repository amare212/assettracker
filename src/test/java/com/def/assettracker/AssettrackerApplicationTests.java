package com.def.assettracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret=dGVzdC1hc3NldHRyYWNrZXItand0LXNlY3JldC1mb3ItdGVzdHMtMzItYnl0ZXM=",
		"spring.datasource.url=jdbc:h2:mem:assettracker;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class AssettrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
