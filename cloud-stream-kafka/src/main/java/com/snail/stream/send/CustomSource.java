package com.snail.stream.send;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CustomSource {

	@Output("output")
    MessageChannel output();
}
