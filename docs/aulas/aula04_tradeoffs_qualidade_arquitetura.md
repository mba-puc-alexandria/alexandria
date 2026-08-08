# 📘 Aula 04 – Trade-offs de Qualidade e Arquitetura de Software

**PUC-SP | Engenharia de Software – ER e APOO**  
**Professor:** Renato Manzan de Andrade  
**Data:** 07 de abril de 2026

---

## 1️⃣ Contexto da Aula

A Aula 04 complementa a discussão sobre **qualidade de software**, aprofundando o conceito de **trade-offs entre atributos de qualidade** e sua relação direta com **arquitetura de software**.

O foco está no **produto de software**, e não no processo de desenvolvimento.

---

## 2️⃣ Qualidade de Software: Necessidades Implícitas e Explícitas

Qualidade está relacionada às necessidades do cliente:

- **Explícitas**: declaradas formalmente;
- **Implícitas**: esperadas, mas nem sempre verbalizadas.

Muitos problemas graves surgem quando necessidades implícitas são ignoradas.

### Exemplos citados em aula
- Equipamentos médicos (raio‑X);
- Sistemas militares;
- Sistemas governamentais (previdência social americana).

---

## 3️⃣ Arquitetura de Software e Objetivos de Negócio

Princípio central enfatizado:

> **Sistemas computacionais suportam objetivos de negócio.**

Esses objetivos:
- direcionam requisitos de qualidade;
- orientam decisões arquiteturais;
- definem prioridades técnicas.

---

## 4️⃣ Exemplo do E‑commerce

O e‑commerce foi utilizado como metáfora para ilustrar a relação entre negócio e arquitetura.

Funcionalidades analisadas:
- busca de produtos;
- navegação por categorias;
- pagamento com cartão de crédito;
- dias de pico (Black Friday, Natal, Dia das Mães).

Esses cenários impactam atributos como:
- escalabilidade;
- segurança;
- desempenho;
- integração com serviços externos.

---

## 5️⃣ Quando o Software “Morre”

A morte do software pode ocorrer por dois motivos principais:

### Técnica
- Não atende mais aos requisitos;
- Custo de manutenção proibitivo.

### Comercial
- Fim de suporte;
- Ausência de atualizações;
- Estratégia de mercado.

É um modelo comum também em hardware e bens de consumo.

---

## 6️⃣ Curva Ideal × Curva Real do Software

### Curva Idealizada
- O software melhora continuamente com o tempo.

### Curva Real
- Aumento da complexidade;
- Dificuldade crescente de manutenção;
- Processo de **entropia do software**.

Esse comportamento depende de:
- arquitetura;
- maturidade do time;
- complexidade do domínio.

---

## 7️⃣ Stakeholders e Usuários

Nem todo stakeholder é usuário e vice‑versa.

- **Stakeholders**: tomadores de decisão, financiadores;
- **Usuários**: utilizam o sistema no dia a dia.

Cada grupo possui uma **percepção diferente de qualidade**.

---

## 8️⃣ Requisitos Funcionais e Não Funcionais

### Requisitos Funcionais
- O que o sistema faz.

### Requisitos Não Funcionais
- Como o sistema faz;
- Atributos de qualidade.

Exemplos discutidos:
- sistema funcional com falha de segurança;
- produto correto entregue fora do prazo;
- aplicação funcional, mas sem escalabilidade.

---

## 9️⃣ Trade-offs de Atributos de Qualidade

Ponto central da aula:

> **Não é possível maximizar todos os atributos de qualidade simultaneamente.**

Exemplos de trade-offs:
- segurança × usabilidade;
- desempenho × escalabilidade;
- custo × qualidade;
- flexibilidade × simplicidade.

Arquitetura é o instrumento para equilibrar essas decisões.

---

## 🧠 Mapa Mental (Resumo)

```
Aula 04 – Trade-offs de Qualidade
│
├── Qualidade
│   ├── Implícita
│   └── Explícita
│
├── Arquitetura
│   └── Orientada ao negócio
│
├── Software
│   ├── Curva ideal
│   └── Curva real (entropia)
│
├── Stakeholders
│   ├── Usuários
│   └── Financiadores
│
└── Trade-offs
    └── Decisões arquiteturais
```

---

## ✅ Conclusão

A Aula 04 consolida a visão de que:

- qualidade é contextual;
- arquitetura envolve escolhas e concessões;
- trade-offs são inevitáveis;
- ignorar requisitos implícitos gera riscos graves.

Esta aula estabelece uma ponte direta entre **Engenharia de Requisitos**, **Arquitetura de Software** e **Orientação a Objetos**.

---
