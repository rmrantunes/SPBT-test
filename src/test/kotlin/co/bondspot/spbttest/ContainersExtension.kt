package co.bondspot.spbttest

import dasniko.testcontainers.keycloak.KeycloakContainer

const val keycloakContainerImage = "quay.io/keycloak/keycloak:26.0.5"
const val keycloakRealmImportFilePath = "/keycloaktc-realm.json"

abstract class KeycloakContainerExtension {
    companion object {
        val KEYCLOAK: KeycloakContainer =
            KeycloakContainer(keycloakContainerImage)
                .withRealmImportFile(keycloakRealmImportFilePath)
                .apply { start() }
    }
}
