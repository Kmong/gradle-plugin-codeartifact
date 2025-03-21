package com.kmong

import software.amazon.awssdk.auth.credentials.*

fun getAwsCredentials(profileName: String?): AwsCredentials {
    return AwsCredentialsProviderChain.builder()
        .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
        .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .addCredentialsProvider(ProfileCredentialsProvider.create(profileName))
        .addCredentialsProvider(ContainerCredentialsProvider.create())
        .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
        .addCredentialsProvider(DefaultCredentialsProvider.create())
        .build().resolveCredentials()
}