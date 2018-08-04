package com.github.fonimus.ssh.shell.postprocess;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.ResultHandler;

import com.github.fonimus.ssh.shell.SshContext;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;

public class TypePostProcessorResultHandler
		implements ResultHandler<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypePostProcessorResultHandler.class);

	private ResultHandler<Object> resultHandler;

	private Map<String, PostProcessor> postProcessorMap = new HashMap<>();

	public TypePostProcessorResultHandler(ResultHandler<Object> resultHandler, List<PostProcessor> postProcessorList) {
		this.resultHandler = resultHandler;
		if (postProcessorList != null) {
			for (PostProcessor postProcessor : postProcessorList) {
				if (this.postProcessorMap.containsKey(postProcessor.getName())) {
					LOGGER.warn("Unable to register post processor for name [{}], it has already been registered", postProcessor.getName());
				} else {
					this.postProcessorMap.put(postProcessor.getName(), postProcessor);
					LOGGER.debug("Post processor with name [{}] registered", postProcessor.getName());
				}
			}
		}
	}

	@Override
	public void handleResult(Object result) {
		if (result == null) {
			return;
		}
		Object obj = result;
		SshContext ctx = SSH_THREAD_CONTEXT.get();
		if (ctx != null && ctx.getPostProcessorsList() != null) {
			for (PostProcessorObject postProcessorObject : ctx.getPostProcessorsList()) {
				String name = postProcessorObject.getName();
				PostProcessor postProcessor = postProcessorMap.get(name);
				if (postProcessor != null && canApply(obj, postProcessor)) {
					LOGGER.debug("Applying post processor [{}]", name);
					try {
						obj = postProcessor.process(obj, postProcessorObject.getParameters());
					} catch (PostProcessorException e) {
						printError(e.getMessage());
						return;
					}
				} else {
					LOGGER.debug("Unknown post processor [{}]", name);
				}
			}
		}
		resultHandler.handleResult(obj);
	}

	private boolean canApply(Object object, PostProcessor postProcessor) {
		Class<?> cls = ((Class) ((ParameterizedType) (postProcessor.getClass().getGenericInterfaces())[0]).getActualTypeArguments()[0]);
		return cls.isAssignableFrom(object.getClass());
	}

	private void printError(String error) {
		resultHandler.handleResult(new AttributedString(error, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)).toAnsi());
	}
}
