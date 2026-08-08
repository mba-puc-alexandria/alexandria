# 📘 Aula 06 – Abstração, Atributos de Qualidade e Gestão da Complexidade

**PUC-SP | Engenharia de Software – ER e APOO**  
**Professor:** Renato Manzan de Andrade  
**Data:** 14 de abril de 2026

---

## 1️⃣ Contexto da Aula

Nesta Aula 06, o professor finaliza o bloco conceitual sobre **atributos de qualidade** e **trade-offs**, fazendo a ponte para **Orientação a Objetos (OO)**. O foco principal está em **abstração** como mecanismo central para **gestão da complexidade** em software.

A aula prepara o terreno para:
- modelagem de objetos;
- pilares da OO;
- integração entre Engenharia de Requisitos e OO.

---

## 2️⃣ Atributos de Qualidade e Subatributos

O professor reforça a importância de termos um **vocabulário comum** quando o cliente menciona atributos como:

- desempenho;
- confiabilidade;
- disponibilidade;
- segurança.

Quando o cliente usa termos genéricos, o papel do engenheiro é **detalhar subatributos** para que seja possível:
- discutir requisitos de forma objetiva;
- comparar alternativas;
- avaliar trade-offs.

---

## 3️⃣ Quantificação dos Atributos

Retomando a aula anterior, o professor destaca a importância de **quantificar** atributos sempre que possível.

> Exemplo: dizer apenas que algo é “importante” não é suficiente.

Analogias mostram que, dependendo do cenário, determinados atributos são **críticos** e outros **secundários**, e isso muda conforme o contexto do sistema.

---

## 4️⃣ Exercício de Abstração por Perspectiva

O professor utiliza três cenários distintos para exercitar abstração:

1. **Atleta**
2. **Paciente em um hospital**
3. **Funcionário de uma empresa (Sistema de RH)**

Embora possam existir **atributos em comum**, cada sistema exige uma **visão específica**.

---

## 5️⃣ Exemplo: Sistema de RH

Ao modelar um objeto que representa um funcionário, atributos relevantes incluem:

- nome;
- cargo;
- salário;
- idade;
- número do PIS;
- data de admissão;
- registro do empregado;
- licenças, férias e afastamentos;
- vínculo sindical;
- departamento atual.

Esses atributos são relevantes **no contexto de RH**, mas não necessariamente em outros sistemas.

---

## 6️⃣ Intersecção de Atributos e Abstração

O professor destaca que:

- existem intersecções entre os atributos dos diferentes cenários;
- porém, cada sistema seleciona apenas o que é **relevante para sua finalidade**.

Isso evidencia o papel da **abstração**:

> escolher o que é importante **e ignorar o restante**.

---

## 7️⃣ Abstração como Gestão da Complexidade

Abstração é apresentada como um dos principais mecanismos usados pelo ser humano para:

- reduzir complexidade;
- focar no essencial;
- tornar sistemas entendíveis e evolutivos.

Mesmo assim, o professor ressalta que:

> **gerenciar a complexidade ainda é difícil**, mesmo com bons mecanismos.

---

## 8️⃣ Encerramento do Bloco Conceitual

Nesta aula, o professor conclui o bloco sobre:

- atributos de qualidade;
- trade-offs;
- abstração.

Esses conceitos são apresentados como pilares tanto para:
- Engenharia de Requisitos;
- Orientação a Objetos.

Na aula seguinte, o foco avançará para:
- modelagem;
- pilares da OO (encapsulamento, abstração, herança/contrato e polimorfismo).

---

## 🧠 Mapa Mental (Resumo)

```
Aula 06 – Abstração
│
├── Atributos de Qualidade
│   ├── Genéricos
│   └── Subatributos
│
├── Quantificação
│   └── Requisitos objetivos
│
├── Perspectiva
│   ├── Atleta
│   ├── Paciente
│   └── Funcionário
│
├── Abstração
│   ├── Selecionar o essencial
│   ├── Ignorar o irrelevante
│
└── Complexidade
    └── Gestão contínua
```

---

## ✅ Conclusão

A Aula 06 consolida a abstração como **habilidade essencial** do engenheiro de software. Antes de escrever código ou escolher padrões, é necessário **entender o domínio**, **definir perspectivas** e **selecionar atributos relevantes**.

Esses conceitos fundamentam todas as decisões técnicas que virão nas próximas aulas.

---
