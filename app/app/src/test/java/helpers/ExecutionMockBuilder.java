package helpers;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class ExecutionMockBuilder {

	private final ExecutionEntity execution;

	public ExecutionMockBuilder() {
		execution = mock(ExecutionEntity.class);
	}

	public <V> ExecutionMockBuilder mockGetVariable(String varName, V val) {
		Mockito.when(execution.hasVariable(varName)).thenReturn(true);
		Mockito.when(execution.getVariable(varName)).thenReturn(val);
		return this;
	}

	public <V> ExecutionMockBuilder mockGetMandatoryVariable(String varName, V val) {
		Mockito.when(execution.hasVariable(varName)).thenReturn(true);
		Mockito.when(execution.getVariable(varName)).thenReturn(val);
		return this;
	}


	public ExecutionMockBuilder mockGetVariableReturnNull(String varName) {
		Mockito.when(execution.hasVariable(varName)).thenReturn(false);
		return this;
	}


	public ExecutionEntity build() {
		return execution;
	}
}
