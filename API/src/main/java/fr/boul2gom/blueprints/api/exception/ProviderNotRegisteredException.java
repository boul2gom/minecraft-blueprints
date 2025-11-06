package fr.boul2gom.blueprints.api.exception;

/**
 * Exception thrown when attempting to access a service provider that has not been registered
 * in the ProviderRegistry.
 *
 * This typically occurs when:
 * - Trying to access Interface.INSTANCE before the provider is registered
 * - Accessing a service before the mod initialization completes
 * - A required provider registration was forgotten during initialization
 */
public class ProviderNotRegisteredException extends BlueprintException {

    private final Class<?> interfaceClass;

    /**
     * Constructs a new ProviderNotRegisteredException for the specified interface class.
     *
     * @param interfaceClass the interface class that was not registered
     */
    public ProviderNotRegisteredException(Class<?> interfaceClass) {
        super(message(interfaceClass));
        this.interfaceClass = interfaceClass;
    }

    /**
     * Constructs a new ProviderNotRegisteredException with a custom message.
     *
     * @param interfaceClass the interface class that was not registered
     * @param message custom error message
     */
    public ProviderNotRegisteredException(Class<?> interfaceClass, String message) {
        super(message);
        this.interfaceClass = interfaceClass;
    }

    /**
     * Constructs a new ProviderNotRegisteredException with a cause.
     *
     * @param interfaceClass the interface class that was not registered
     * @param cause the cause of the exception
     */
    public ProviderNotRegisteredException(Class<?> interfaceClass, Throwable cause) {
        super(message(interfaceClass), cause);
        this.interfaceClass = interfaceClass;
    }

    /**
     * Gets the interface class that was not registered.
     *
     * @return the interface class
     */
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    private static String message(Class<?> interfaceClass) {
        final String className = interfaceClass != null ? interfaceClass.getName() : "null";
        return String.format(
            "No provider registered for interface: %s. " +
            "Ensure that the provider is registered during mod initialization " +
            "before accessing %s.INSTANCE",
            className,
            interfaceClass != null ? interfaceClass.getSimpleName() : "Interface"
        );
    }
}
