package com.kmong

import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

fun getAwsCredentials(profileName: String?): AwsCredentials {
    return AwsCredentialsProviderChain.builder()
        .addCredentialsProvider(ProfileCredentialsProvider.create(profileName))
        .addCredentialsProvider(DefaultCredentialsProvider.create())
        .addCredentialsProvider(ContainerCredentialsProvider.create())
        .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
        .build().resolveCredentials()
}