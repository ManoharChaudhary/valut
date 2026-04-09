package com.vault.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RuleAdminServiceTest {
	private static final ObjectMapper OM = new ObjectMapper();

	@Mock
	private RuleRepository ruleRepository;

	@Mock
	private RuleVersionRepository ruleVersionRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private RuleAdminService ruleAdminService;

	@Test
	void appendVersionPublishesRuleUpdatedEvent() throws Exception {
		Rule rule = new Rule();
		rule.setId(10L);
		rule.setFeatureKey("flags.dark_mode");

		when(ruleRepository.findById(10L)).thenReturn(Optional.of(rule));
		when(ruleVersionRepository.save(any(RuleVersion.class))).thenAnswer(inv -> {
			RuleVersion v = inv.getArgument(0);
			v.setId(555L);
			return v;
		});

		JsonNode cond = OM.readTree("{\"allow\":true}");
		RuleVersion saved = ruleAdminService.appendRuleVersion(10L, cond, null, "tester");

		assertThat(saved.getId()).isEqualTo(555L);

		ArgumentCaptor<RuleUpdatedEvent> captor = ArgumentCaptor.forClass(RuleUpdatedEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertThat(captor.getValue().featureKey()).isEqualTo("flags.dark_mode");
		assertThat(captor.getValue().ruleId()).isEqualTo(10L);
	}
}
