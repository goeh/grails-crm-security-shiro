import org.apache.shiro.authc.credential.Sha512CredentialsMatcher

beans = {

    credentialMatcher(Sha512CredentialsMatcher) {
        storedCredentialsHexEncoded = true
        hashSalted = true
        hashIterations = 1000
    }
}