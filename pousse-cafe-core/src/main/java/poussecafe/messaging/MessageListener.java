package poussecafe.messaging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import poussecafe.process.ProcessManagerKey;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;
import static poussecafe.check.Predicates.emptyOrNullString;
import static poussecafe.check.Predicates.not;

public class MessageListener {

    private String listenerId;

    private Method method;

    private Object target;

    public MessageListener(String listenerId, Method method, Object target) {
        setListenerId(listenerId);
        setMethod(method);
        setTarget(target);
    }

    public Method getMethod() {
        return method;
    }

    private void setMethod(Method method) {
        checkThat(value(method).notNull().because("Method cannot be null"));
        this.method = method;
    }

    public Object getTarget() {
        return target;
    }

    private void setTarget(Object target) {
        checkThat(value(target).notNull().because("Target cannot be null"));
        this.target = target;
    }

    public String getListenerId() {
        return listenerId;
    }

    private void setListenerId(String listenerId) {
        checkThat(value(listenerId).verifies(not(emptyOrNullString())).because("Listener ID cannot be empty"));
        this.listenerId = listenerId;
    }

    public ProcessManagerKey consume(Message message) {
        try {
            return (ProcessManagerKey) method.invoke(target, message);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new MessageConsumptionException("Unable to invoke listener", e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageListener other = (MessageListener) obj;
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MessageListener [listenerId=" + listenerId + ", method=" + method + ", target=" + target + "]";
    }

}