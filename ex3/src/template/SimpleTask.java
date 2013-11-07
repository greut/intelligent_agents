package template;

import logist.task.Task;

public class SimpleTask {
	private Task task;
	private Action action;

	public SimpleTask(Task task, Action action) {
		this.task = task;
		this.action = action;
	}

	public Task getTask() {
		return task;
	}

	public Action getAction() {
		return action;
	}
}