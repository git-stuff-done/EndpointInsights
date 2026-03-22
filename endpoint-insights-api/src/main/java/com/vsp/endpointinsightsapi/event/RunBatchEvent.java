package com.vsp.endpointinsightsapi.event;

import com.vsp.endpointinsightsapi.model.TestBatch;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class RunBatchEvent extends ApplicationEvent {

	@Getter
	private final TestBatch batch;


	public RunBatchEvent(Object source, TestBatch batch) {
		super(source);
		this.batch = batch;
	}
}
