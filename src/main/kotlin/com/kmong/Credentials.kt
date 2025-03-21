package com.kmong

import software.amazon.awssdk.auth.credentials.*

fun getAwsCredentials(profileName: String?): AwsCredentials {
    return AwsCredentialsProviderChain.builder()
        .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
        .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .addCredentialsProvider(ContainerCredentialsProvider.create())
        .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
        .addCredentialsProvider(ProfileCredentialsProvider.create(profileName))
        .addCredentialsProvider(DefaultCredentialsProvider.create())
        .build().resolveCredentials()
}