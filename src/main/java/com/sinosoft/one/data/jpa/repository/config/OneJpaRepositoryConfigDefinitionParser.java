/*
 * Copyright 2008-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sinosoft.one.data.jpa.repository.config;

import com.sinosoft.one.data.jade.context.spring.JadeBeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import com.sinosoft.one.data.jpa.repository.config.OneSimpleJpaRepositoryConfiguration.JpaRepositoryConfiguration;
import org.springframework.data.repository.config.AbstractRepositoryConfigDefinitionParser;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser to create bean definitions for repositories namespace. Registers bean definitions for repositories as well as
 * {@code PersistenceAnnotationBeanPostProcessor} and {@code PersistenceExceptionTranslationPostProcessor} to
 * transparently inject entity manager factory instance and apply exception translation.
 * <p>
 * The definition parser allows two ways of configuration. Either it looks up the manually defined repository instances
 * or scans the defined domain package for candidates for repositories.
 * 
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Gil Markham
 */
class OneJpaRepositoryConfigDefinitionParser extends
		OneAbstractRepositoryConfigDefinitionParser<OneSimpleJpaRepositoryConfiguration, JpaRepositoryConfiguration> {

	private static final Class<?> PAB_POST_PROCESSOR = PersistenceAnnotationBeanPostProcessor.class;
	private static final Class<?> PET_POST_PROCESSOR = PersistenceExceptionTranslationPostProcessor.class;
	private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.AbstractRepositoryConfigDefinitionParser#getGlobalRepositoryConfigInformation(org.w3c.dom.Element)
	 */

	protected OneSimpleJpaRepositoryConfiguration getGlobalRepositoryConfigInformation(Element element) {

		return new OneSimpleJpaRepositoryConfiguration(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.AbstractRepositoryConfigDefinitionParser#postProcessBeanDefinition(org.springframework.data.repository.config.SingleRepositoryConfigInformation, org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
	 */

	protected void postProcessBeanDefinition(JpaRepositoryConfiguration ctx, BeanDefinitionBuilder builder,
			BeanDefinitionRegistry registry, Object beanSource) {

		String transactionManagerRef = StringUtils.hasText(ctx.getTransactionManagerRef()) ? ctx.getTransactionManagerRef()
				: DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;
		builder.addPropertyValue("transactionManager", transactionManagerRef);

		String entityManagerRef = ctx.getEntityManagerFactoryRef();

		if (StringUtils.hasText(entityManagerRef)) {
			builder.addPropertyValue("entityManager", getEntityManagerBeanDefinitionFor(entityManagerRef, beanSource));
		}
	}

	/**
	 * Creates an anonymous factory to extract the actual {@link javax.persistence.EntityManager} from the
	 * {@link javax.persistence.EntityManagerFactory} bean name reference.
	 * 
	 * @param entityManagerFactoryBeanName
	 * @param source
	 * @return
	 */
	private BeanDefinition getEntityManagerBeanDefinitionFor(String entityManagerFactoryBeanName, Object source) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.rootBeanDefinition("org.springframework.orm.jpa.SharedEntityManagerCreator");
		builder.setFactoryMethod("createSharedEntityManager");
		builder.addConstructorArgReference(entityManagerFactoryBeanName);

		AbstractBeanDefinition bean = builder.getRawBeanDefinition();
		bean.setSource(source);

		return bean;
	}

	/**
	 * Registers an additional {@link org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor} to
	 * trigger automatic injextion of {@link javax.persistence.EntityManager} .
	 * 
	 * @param registry
	 * @param source
	 */

	protected void registerBeansForRoot(BeanDefinitionRegistry registry, Object source) {

		super.registerBeansForRoot(registry, source);

		if (!hasBean(PET_POST_PROCESSOR, registry)) {

			AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(PET_POST_PROCESSOR)
					.getBeanDefinition();

			registerWithSourceAndGeneratedBeanName(registry, definition, source);
		}

		if (!hasBean(PAB_POST_PROCESSOR, registry)) {

			AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(PAB_POST_PROCESSOR)
					.getBeanDefinition();

			registerWithSourceAndGeneratedBeanName(registry, definition, source);
		}

        //register JadeBean
        AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(JadeBeanFactoryPostProcessor.class)
                .getBeanDefinition();
        registerWithSourceAndGeneratedBeanName(registry, definition, source);
	}
}
