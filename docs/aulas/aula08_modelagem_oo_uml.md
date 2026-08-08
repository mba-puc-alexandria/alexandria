# 📘 Aula 08 – Modelagem Orientada a Objetos, Evolução das Linguagens e UML

**PUC-SP | Engenharia de Software – ER e APOO**  
**Professor:** Renato Manzan de Andrade  
**Data:** 28 de abril de 2026

---

## 1️⃣ Contexto da Aula

A Aula 08 retoma formalmente o tema de **Orientação a Objetos (OO)**, conectando conceitos históricos, técnicos e práticos. A aula faz a ponte entre:

- evolução das linguagens de programação;
- diferentes **topologias de organização de software**;
- surgimento da **UML** como linguagem padrão de modelagem;
- escolha consciente de técnicas de modelagem e nível de rigor.

Antes de avançar nos pilares da OO, o professor reforça a importância de compreender **de onde esses conceitos vieram**.

---

## 2️⃣ Evolução Histórica das Linguagens de Programação

O professor apresenta uma linha evolutiva das linguagens, passando por:

- linguagens procedurais clássicas;
- Visual Basic;
- Java;
- Python;
- Java EE;
- .NET como framework corporativo.

Ferramentas da **Borland** (como Delphi) são citadas como exemplos de ambientes de desenvolvimento muito avançados para a época.

---

## 3️⃣ Topologias de Organização de Software

A aula apresenta diferentes **topologias de organização**, mostrando como dados e funcionalidades evoluíram ao longo do tempo.

### Primeiras gerações

- grande repositório de dados centralizado;
- subprogramas acessando diretamente os dados;
- forte acoplamento.

Hoje, essa abordagem pode ser comparada conceitualmente a um **data lake sem governança**.

---

### Evolução para módulos e componentes

- introdução de componentes dentro dos subprogramas;
- início da modularização;
- separação gradual de responsabilidades.

---

### Orientação a Objetos

- objetos com **atributos e operações**;
- dados encapsulados dentro das instâncias;
- objetos interagindo entre si;
- menor acoplamento e melhor organização.

---

### Componentização

- sistemas organizados em componentes;
- encapsulamento mais forte;
- interação entre componentes bem definidos;
- base para sistemas de médio e grande porte.

---

## 4️⃣ Dados Dentro dos Objetos

Um ponto enfatizado na aula:

> Na Orientação a Objetos, os dados deixam de ser globais e passam a viver **dentro dos objetos**.

Cada objeto (instância de uma classe) possui:
- estado (atributos);
- comportamento (operações).

Isso representa um avanço significativo em relação às arquiteturas anteriores.

---

## 5️⃣ A “Guerra dos Metodologistas”

Na década de 1990, surgiram diversos métodos de OO, conhecidos como a **guerra dos métodos**.

Principais nomes citados:

- **Grady Booch** – foco em design;
- **James Rumbaugh** – OMT (Object Modeling Technique), foco em análise;
- **Ivar Jacobson** – OOSE (Object‑Oriented Software Engineering), foco em casos de uso.

Os métodos eram muito semelhantes, mas utilizavam **notações diferentes**, gerando confusão no mercado.

---

## 6️⃣ Surgimento da UML

Para resolver a fragmentação, os três metodologistas se uniram, dando origem à **UML (Unified Modeling Language)**.

### Marcos importantes

- 1995: versão beta (≈ 0.8);
- 1996: UML 1.0;
- 1997: adoção oficial pela **OMG (Object Management Group)**;
- evolução contínua até versões atuais (≈ UML 2.5).

A UML incorporou feedbacks da:
- academia;
- indústria.

---

## 7️⃣ Ferramentas CASE

Com a consolidação da UML, surgiram ferramentas **CASE (Computer Aided Software Engineering)**.

Exemplo citado:
- **Rational Rose**, posteriormente adquirido pela IBM.

Essas ferramentas permitiam:
- modelagem visual;
- apoio ao projeto de software;
- padronização da documentação.

---

## 8️⃣ Orientação a Objetos como Evolução Natural

A aula reforça que a OO:

- não surgiu de forma abrupta;
- é uma evolução da análise estruturada;
- acompanhou a evolução das linguagens e da indústria.

A mudança foi **gradual**, tanto em conceitos quanto em ferramentas.

---

## 9️⃣ Técnica de Modelagem × Nível de Rigor

Ponto central de fechamento da aula:

> **Qual técnica de modelagem usar e com qual nível de rigor?**

A resposta depende de:
- objetivo do sistema;
- tamanho do projeto;
- maturidade do time;
- tipo de problema.

Modelar demais gera desperdício. Modelar de menos gera riscos.

---

## 🧠 Mapa Mental (Resumo)

```
Aula 08 – Modelagem OO e UML
│
├── Evolução das Linguagens
│
├── Topologias de Software
│   ├── Dados centralizados
│   ├── Módulos
│   ├── Objetos
│   └── Componentes
│
├── Orientação a Objetos
│   ├── Atributos
│   └── Operações
│
├── UML
│   ├── Booch
│   ├── Rumbaugh
│   └── Jacobson
│
├── Ferramentas CASE
│
└── Modelagem
    └── Técnica × rigor
```

---

## ✅ Conclusão

A Aula 08 consolida a base histórica e conceitual da **Orientação a Objetos**, preparando o terreno para o estudo detalhado dos **pilares da OO**.

Compreender a evolução das linguagens, das topologias e da UML permite escolher **melhores soluções**, aplicar **modelagem de forma consciente** e evitar decisões dogmáticas.

---
