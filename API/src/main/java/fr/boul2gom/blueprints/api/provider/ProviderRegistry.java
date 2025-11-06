package fr.boul2gom.blueprints.api.provider;

import fr.boul2gom.blueprints.api.exception.ProviderNotRegisteredException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The ProviderRegistry class provides a centralized, thread-safe registry for managing
 * service providers using lazy initialization. It allows registration of interface-to-implementation
 * bindings via suppliers, and ensures that instances are created only when first accessed.
 *
 * Key Features:
 * - Lazy initialization: instances are created only on first access
 * - Singleton pattern: one instance per interface type
 * - Support for unregistration and introspection
 *
 * Thread-safety:
 * - Registration happens only during mod initialization (single-threaded), so suppliers map uses HashMap
 * - Lazy initialization via get() can happen from multiple threads, so instances map uses ConcurrentHashMap
 * - Methods that access suppliers are synchronized for safety
 *
 * Typical Usage:
 * 1. Register a provider: ProviderRegistry.register(IMyService.class, MyServiceImpl::new)
 * 2. Access via interface: IMyService.INSTANCE (where INSTANCE = ProviderRegistry.get(IMyService.class))
 * 3. Check registration: ProviderRegistry.isRegistered(IMyService.class)
 */
public class ProviderRegistry {

    // Map storing the suppliers for lazy instantiation
    // HashMap is sufficient since registration only happens during single-threaded mod init
    private static final Map<Class<?>, Supplier<?>> SUPPLIERS = new HashMap<>();

    // Map storing the already-instantiated singleton instances
    // ConcurrentHashMap needed because lazy init can happen from multiple threads
    private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Registers a service provider for the given interface class.
     * The supplier will be invoked lazily when the service is first accessed via get().
     *
     * @param interfaceClass the interface class to register
     * @param supplier the supplier providing the implementation instance
     * @param <T> the type of the service interface
     * @throws IllegalArgumentException if interfaceClass or supplier is null
     * @throws IllegalStateException if a provider is already registered for this interface
     */
    public static synchronized <T> void register(Class<T> interfaceClass, Supplier<T> supplier) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class cannot be null");
        }
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        if (SUPPLIERS.containsKey(interfaceClass)) {
            throw new IllegalStateException(
                String.format("Provider already registered for interface: %s", interfaceClass.getName())
            );
        }

        SUPPLIERS.put(interfaceClass, supplier);
    }

    /**
     * Retrieves the singleton instance for the given interface class.
     * If the instance doesn't exist yet, it will be created using the registered supplier.
     *
     * This method is thread-safe and ensures only one instance is created per interface.
     *
     * @param interfaceClass the interface class to get the instance for
     * @param <T> the type of the service interface
     * @return the singleton instance of the service
     * @throws ProviderNotRegisteredException if no provider is registered for the interface
     * @throws IllegalArgumentException if interfaceClass is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class cannot be null");
        }

        // computeIfAbsent ensures thread-safe lazy initialization
        return (T) INSTANCES.computeIfAbsent(interfaceClass, key -> {
            final Supplier<?> supplier;
            // Synchronize access to suppliers map
            synchronized (ProviderRegistry.class) {
                supplier = SUPPLIERS.get(key);
            }
            if (supplier == null) {
                throw new ProviderNotRegisteredException(interfaceClass);
            }
            return supplier.get();
        });
    }

    /**
     * Checks if a provider is registered for the given interface class.
     * Note: This only checks if a supplier is registered, not if the instance has been created.
     *
     * @param interfaceClass the interface class to check
     * @return true if a provider is registered, false otherwise
     */
    public static synchronized boolean isRegistered(Class<?> interfaceClass) {
        return interfaceClass != null && SUPPLIERS.containsKey(interfaceClass);
    }

    /**
     * Checks if an instance has already been created for the given interface class.
     *
     * @param interfaceClass the interface class to check
     * @return true if the instance has been initialized, false otherwise
     */
    public static boolean isInitialized(Class<?> interfaceClass) {
        return interfaceClass != null && INSTANCES.containsKey(interfaceClass);
    }

    /**
     * Unregisters a provider and removes its cached instance.
     * This operation is thread-safe but should be used carefully as it may break
     * existing references to the INSTANCE field in interfaces.
     *
     * @param interfaceClass the interface class to unregister
     */
    public static synchronized void unregister(Class<?> interfaceClass) {
        if (interfaceClass != null) {
            SUPPLIERS.remove(interfaceClass);
            INSTANCES.remove(interfaceClass);
        }
    }

    /**
     * Clears all registered providers and cached instances.
     * Use with caution: this will break all existing INSTANCE references.
     * Primarily useful for testing or during application shutdown.
     */
    public static synchronized void clear() {
        SUPPLIERS.clear();
        INSTANCES.clear();
    }
}
